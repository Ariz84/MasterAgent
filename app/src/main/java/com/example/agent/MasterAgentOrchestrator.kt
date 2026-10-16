package com.example.agent

import com.example.model.SpecialistAgentType
import com.example.network.GeminiClient
import com.example.network.TermuxConnector

data class OrchestrationResult(
    val masterResponseText: String,
    val delegatedAgent: SpecialistAgentType = SpecialistAgentType.MASTER,
    val statusTag: String = "SUCCESS",
    val actionType: String? = null,
    val actionPayload: String? = null
)

class MasterAgentOrchestrator(
    private val geminiClient: GeminiClient,
    private val termuxConnector: TermuxConnector
) {

    suspend fun getProactiveGreeting(): String {
        return "Assalam-o-alaikum! Main aapka Master AI Agent hoon. Termux backend connected hai, batayein aaj kya kaam karna hai?"
    }

    suspend fun processUserInstruction(instruction: String): OrchestrationResult {
        // Direct route to Termux if enabled
        if (termuxConnector.isEnabled) {
            val termuxResult = termuxConnector.sendChatMessage(instruction)
            if (termuxResult.isSuccess) {
                val responseText = termuxResult.getOrThrow()
                return OrchestrationResult(
                    masterResponseText = responseText,
                    delegatedAgent = SpecialistAgentType.MASTER,
                    statusTag = "TERMUX_BACKEND"
                )
            }
        }

        // Fallback to direct Gemini if Termux is unavailable
        val geminiResp = geminiClient.generateResponse(instruction)
        return OrchestrationResult(
            masterResponseText = geminiResp,
            delegatedAgent = SpecialistAgentType.MASTER,
            statusTag = "GEMINI_DIRECT"
        )
    }
}
