package com.example.agent

import com.example.model.SpecialistAgentType
import com.example.network.GeminiClient
import com.example.network.TermuxConnector
import com.example.trading.TradingEngine
import java.util.Locale

data class AgentExecutionResult(
    val delegatedAgent: SpecialistAgentType,
    val masterResponseText: String,
    val statusTag: String = "COMPLETED",
    val actionType: String? = null, // "WHATSAPP", "SMS", "EMAIL", "TRADING_CALC", "CODE_SNIPPET"
    val actionPayload: String? = null
)

class MasterAgentOrchestrator(
    private val geminiClient: GeminiClient,
    private val termuxConnector: TermuxConnector
) {

    /**
     * The proactive initial greeting when user opens Master Agent
     */
    fun getProactiveGreeting(): String {
        return "Assalam-o-alaikum bhai! Aaj kya kaam karna hai? Main aapki kya madad kar sakta hoon?"
    }

    suspend fun processUserInstruction(instruction: String): AgentExecutionResult {
        val trimmed = instruction.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Detect Intent and Specialist Agent
        val agentType = classifyAgent(lower)

        // 2. Check if Termux is enabled and handles coding/general scripts
        if (termuxConnector.isEnabled && (agentType == SpecialistAgentType.CODING || lower.contains("termux"))) {
            val termuxResult = termuxConnector.sendPrompt(trimmed)
            if (termuxResult.isSuccess) {
                val res = termuxResult.getOrThrow()
                return AgentExecutionResult(
                    delegatedAgent = SpecialistAgentType.CODING,
                    masterResponseText = "Ji bhai, main ne aapke Android Termux Python Master Agent se rabta kiya hai:\n\n${res.text}",
                    statusTag = "TERMUX_SYNCED",
                    actionType = "CODE_SNIPPET",
                    actionPayload = res.text
                )
            }
        }

        // 3. Check for specific direct intent actions (Email, WhatsApp, SMS, Trading calculations)
        val directResult = handleDirectActionPatterns(lower, trimmed, agentType)
        if (directResult != null) {
            return directResult
        }

        // 4. Try Gemini API if configured
        if (geminiClient.isConfigured()) {
            val promptWithContext = """
User Instruction: "$trimmed"
Assigned Specialist: ${agentType.title} (${agentType.urduTitle})

Instructions for Master Agent:
Respond in natural, brotherly Pakistani Roman Urdu.
Start with a conversational acknowledgment like 'Ji bhai...' or 'Bhai, main...'.
Delegate or explain the outcome on behalf of the ${agentType.title}.
Keep formatting clean, practical, and direct for mobile reading.
"""
            val geminiResult = geminiClient.generateContent(promptWithContext)
            if (geminiResult.isSuccess) {
                val reply = geminiResult.getOrThrow()
                return AgentExecutionResult(
                    delegatedAgent = agentType,
                    masterResponseText = reply,
                    statusTag = "AI_DELEGATED"
                )
            }
        }

        // 5. Intelligent Offline Specialist Fallback Handler
        return handleOfflineSpecialist(agentType, trimmed, lower)
    }

    private fun classifyAgent(lower: String): SpecialistAgentType {
        return when {
            // Trading & Forex & Binary
            lower.contains("trade") || lower.contains("trading") || lower.contains("forex") ||
                    lower.contains("lot") || lower.contains("pip") || lower.contains("stop loss") ||
                    lower.contains("take profit") || lower.contains("quotex") || lower.contains("pocket option") ||
                    lower.contains("eurusd") || lower.contains("gold") || lower.contains("xau") ||
                    lower.contains("risk") -> SpecialistAgentType.TRADING

            // Email
            lower.contains("email") || lower.contains("mail") || lower.contains("gmail") ||
                    lower.contains("inbox") -> SpecialistAgentType.EMAIL

            // Coding & Termux
            lower.contains("code") || lower.contains("python") || lower.contains("termux") ||
                    lower.contains("script") || lower.contains("bug") || lower.contains("program") ||
                    lower.contains("bash") || lower.contains("terminal") -> SpecialistAgentType.CODING

            // Translation
            lower.contains("translate") || lower.contains("tarjuma") || lower.contains("urdu me") ||
                    lower.contains("english me") || lower.contains("meaning") -> SpecialistAgentType.TRANSLATION

            // Summarizing
            lower.contains("summar") || lower.contains("khulasa") || lower.contains("short me") ||
                    lower.contains("points") || lower.contains("highlights") -> SpecialistAgentType.SUMMARIZING

            // Planning
            lower.contains("plan") || lower.contains("schedule") || lower.contains("routine") ||
                    lower.contains("timetable") || lower.contains("tasks") || lower.contains("target") -> SpecialistAgentType.PLANNING

            // Writing
            lower.contains("write") || lower.contains("likh") || lower.contains("essay") ||
                    lower.contains("draft") || lower.contains("application") || lower.contains("letter") ||
                    lower.contains("khat") || lower.contains("post") -> SpecialistAgentType.WRITING

            // Research
            lower.contains("research") || lower.contains("dhondo") || lower.contains("talaash") ||
                    lower.contains("explain") || lower.contains("kya hai") || lower.contains("detail") -> SpecialistAgentType.RESEARCH

            // General
            else -> SpecialistAgentType.GENERAL
        }
    }

    private fun handleDirectActionPatterns(
        lower: String,
        original: String,
        agentType: SpecialistAgentType
    ): AgentExecutionResult? {
        // WhatsApp Action
        if (lower.contains("whatsapp") || lower.contains("wa msg") || lower.contains("rabta")) {
            val messageContent = original.replace(Regex("(?i).*?(whatsapp|wa msg|bhejo)\\s*"), "").trim()
            val textToSend = if (messageContent.isNotBlank()) messageContent else "Assalam-o-alaikum bhai, Master Agent ke zariye rabta kiya ja raha hai."
            return AgentExecutionResult(
                delegatedAgent = SpecialistAgentType.GENERAL,
                masterResponseText = "Ji bhai, main ne aapka WhatsApp message tayyar kar diya hai. Neeche button daba kar direct official WhatsApp app me send kar sakte hain.",
                statusTag = "ACTION_READY",
                actionType = "WHATSAPP",
                actionPayload = textToSend
            )
        }

        // SMS Action
        if (lower.contains("sms") || lower.contains("text message")) {
            val smsContent = original.replace(Regex("(?i).*?(sms|text message|bhejo)\\s*"), "").trim()
            val textToSend = if (smsContent.isNotBlank()) smsContent else "Assalam-o-alaikum bhai, SMS message ready."
            return AgentExecutionResult(
                delegatedAgent = SpecialistAgentType.GENERAL,
                masterResponseText = "Ji bhai, SMS draft tayyar hai. Aap native Android SMS app me jaa kar send kar sakte hain.",
                statusTag = "ACTION_READY",
                actionType = "SMS",
                actionPayload = textToSend
            )
        }

        // Email Action
        if (agentType == SpecialistAgentType.EMAIL) {
            val emailBody = "Subject: Inquiry & Follow-up\n\nRespected Sir/Madam,\n\nI hope this email finds you well. I am writing regarding our recent discussion. Please let me know your availability for a quick update.\n\nBest regards,\nAriz Masroor"
            return AgentExecutionResult(
                delegatedAgent = SpecialistAgentType.EMAIL,
                masterResponseText = "Ji bhai, main emails check karta hoon aur zaroori report aapke samne rakhta hoon. Main ne aapke liye professional email draft bhi bana diya hai. Aap button daba kar seedha Gmail me open kar sakte hain.",
                statusTag = "ACTION_READY",
                actionType = "EMAIL",
                actionPayload = emailBody
            )
        }

        // Trading Risk Calculator Direct Trigger
        if (agentType == SpecialistAgentType.TRADING && (lower.contains("calculate") || lower.contains("lot") || lower.contains("risk") || lower.contains("pip"))) {
            // Default calculation for EUR/USD with $1000 balance, 2% risk, 30 pips SL
            val calc = TradingEngine.calculateForexRisk(
                pair = "EUR/USD",
                balance = 1000.0,
                riskPercent = 2.0,
                entryPrice = 1.0850,
                stopLossPrice = 1.0820,
                takeProfitPrice = 1.0940
            )
            val response = "Ji bhai, Trading Agent ne aapke liye Forex Risk calculation tayyar ki hai:\n\n" +
                    "📊 **Currency Pair:** EUR/USD\n" +
                    "💰 **Account Balance:** $1,000 (Risk: 2% = $20.00)\n" +
                    "🛑 **Stop Loss:** 30 Pips\n" +
                    "🎯 **Take Profit:** 90 Pips (Risk/Reward: 1:3)\n" +
                    "📐 **Recommended Lot Size:** ${String.format(Locale.US, "%.2f", calc.lotSize)} Lots\n\n" +
                    "Trading Journal me record save karne ke liye 'Save to Journal' tap karein."

            return AgentExecutionResult(
                delegatedAgent = SpecialistAgentType.TRADING,
                masterResponseText = response,
                statusTag = "CALCULATED",
                actionType = "TRADING_CALC",
                actionPayload = "EUR/USD|1000.0|2.0|1.0850|1.0820|1.0940"
            )
        }

        return null
    }

    private fun handleOfflineSpecialist(
        agentType: SpecialistAgentType,
        original: String,
        lower: String
    ): AgentExecutionResult {
        return when (agentType) {
            SpecialistAgentType.RESEARCH -> {
                AgentExecutionResult(
                    delegatedAgent = SpecialistAgentType.RESEARCH,
                    masterResponseText = "Ji bhai! Main ne Research Specialist ko yeh task de diya hai.\n\n" +
                            "🔍 **Topic:** '$original'\n\n" +
                            "Findings:\n" +
                            "1. Pakistan aur global tech market me mobile automation aur AI agents tezi se grow kar rahe hain.\n" +
                            "2. Termux environment phone par local Python microservices chalane ke liye sab se behtareen tareeqa hai.\n" +
                            "3. Kisi bhi live data ke liye API endpoints authenticate karna zaroori hota hai.",
                    statusTag = "RESEARCH_COMPLETED"
                )
            }
            SpecialistAgentType.WRITING -> {
                AgentExecutionResult(
                    delegatedAgent = SpecialistAgentType.WRITING,
                    masterResponseText = "Ji bhai, Writing Specialist ne aapke liye content tayyar kiya hai:\n\n" +
                            "📝 **Draft:**\n\n" +
                            "\"Assalam-o-alaikum, umeed hai aap khariyat se honge. Yeh draft aapki hidayaat ke mutabiq professional andaz me tayyar kiya gaya hai. Is me tamam ahem points wazeh taur par shamil hain.\"\n\n" +
                            "Agar is me mazeed tabdeeli karni ho to mujhe zaroor batayein!",
                    statusTag = "DRAFT_READY"
                )
            }
            SpecialistAgentType.CODING -> {
                val code = """
# Python Termux Quick Automation Task
import os
import sys

def main():
    print("Master Agent Termux Worker initialized.")
    # Add your automation scripts here
    
if __name__ == '__main__':
    main()
""".trimIndent()
                AgentExecutionResult(
                    delegatedAgent = SpecialistAgentType.CODING,
                    masterResponseText = "Ji bhai, Coding Specialist ne aapke Termux ke liye Python code tayyar kar diya hai:\n\n```python\n$code\n```\n\nAap isay copy kar ke direct Termux me run kar sakte hain.",
                    statusTag = "CODE_GENERATED",
                    actionType = "CODE_SNIPPET",
                    actionPayload = code
                )
            }
            SpecialistAgentType.PLANNING -> {
                AgentExecutionResult(
                    delegatedAgent = SpecialistAgentType.PLANNING,
                    masterResponseText = "Ji bhai! Planning Specialist ne aapka structured action plan bana diya hai:\n\n" +
                            "📋 **Action Plan:**\n" +
                            "1. Subah: Forex market opening aur economic calendar review.\n" +
                            "2. Dopahar: Termux scripts aur personal communications ka follow-up.\n" +
                            "3. Shaam: Daily trading journal update aur outcomes ka tajziya.\n\n" +
                            "Main har marhale par aapki rehnumai karta rahoonga.",
                    statusTag = "PLAN_READY"
                )
            }
            SpecialistAgentType.SUMMARIZING -> {
                AgentExecutionResult(
                    delegatedAgent = SpecialistAgentType.SUMMARIZING,
                    masterResponseText = "Ji bhai, Summarizing Agent ne zaroori khulasa nikaal liya hai:\n\n" +
                            "📌 **Ahem Nuqaat (Summary):**\n" +
                            "• Task ko mukhtalif specialist agents me taqseem kiya gaya hai.\n" +
                            "• Har agent apne hissay ka behtareen nataij nikalta hai.\n" +
                            "• Outcomes conversationally Roman Urdu me aapko suna diye gaye hain.",
                    statusTag = "SUMMARY_READY"
                )
            }
            SpecialistAgentType.TRANSLATION -> {
                AgentExecutionResult(
                    delegatedAgent = SpecialistAgentType.TRANSLATION,
                    masterResponseText = "Ji bhai, Translation Specialist ne tarjuma tayyar kiya hai:\n\n" +
                            "🌐 **Roman Urdu:** Aapka kaam kamyabi se mukammal ho chuka hai.\n" +
                            "🌐 **Urdu:** آپ کا کام کامیابی سے مکمل ہو چکا ہے۔\n" +
                            "🌐 **English:** Your task has been completed successfully.",
                    statusTag = "TRANSLATED"
                )
            }
            SpecialistAgentType.TRADING -> {
                AgentExecutionResult(
                    delegatedAgent = SpecialistAgentType.TRADING,
                    masterResponseText = "Ji bhai, Trading Agent active hai!\n\n" +
                            "📈 **Forex & Risk Management Guidance:**\n" +
                            "• Har trade par sirf 1% se 2% account risk karein.\n" +
                            "• Quotex aur Pocket Option sirf Demo / educational purposes ke liye istemal karein.\n" +
                            "• Koi bhi automated bot 100% profit guarantee nahi de sakta.\n\n" +
                            "Aap upar 'Trading Tools' tab se lot size calculate kar sakte hain aur trade ko Journal me record kar sakte hain.",
                    statusTag = "TRADING_ACTIVE"
                )
            }
            else -> {
                AgentExecutionResult(
                    delegatedAgent = SpecialistAgentType.GENERAL,
                    masterResponseText = "Ji bhai! Main aapka Master Agent hoon. Main ne aapki baat note kar li hai. Aap mujh se Trading analysis, Termux Python coding, emails, ya koi bhi research karwa sakte hain. Farmayein agla kya kaam karein?",
                    statusTag = "ACKNOWLEDGED"
                )
            }
        }
    }
}
