package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

enum class SpecialistAgentType(
    val id: String,
    val title: String,
    val urduTitle: String,
    val description: String,
    val color: Color
) {
    MASTER(
        id = "MASTER",
        title = "Master Agent",
        urduTitle = "مرکزی آرکیسٹریٹر (Master)",
        description = "Central coordinator routing instructions, delegating tasks and reporting back.",
        color = AgentMasterColor
    ),
    RESEARCH(
        id = "RESEARCH",
        title = "Research Agent",
        urduTitle = "تحقیق اسپیشلسٹ",
        description = "Deep information retrieval, fact checking, and market landscape analysis.",
        color = AgentResearchColor
    ),
    WRITING(
        id = "WRITING",
        title = "Writing Agent",
        urduTitle = "تحریر و بلاگ اسپیشلسٹ",
        description = "Composing essays, scripts, Roman Urdu & English letters and proposals.",
        color = AgentWritingColor
    ),
    CODING(
        id = "CODING",
        title = "Coding Agent",
        urduTitle = "کوڈنگ و ٹرمکس اسپیشلسٹ",
        description = "Termux Python scripts, automation code, bug fixes and mobile scripts.",
        color = AgentCodingColor
    ),
    PLANNING(
        id = "PLANNING",
        title = "Planning Agent",
        urduTitle = "منصوبہ بندی اسپیشلسٹ",
        description = "Actionable roadmaps, daily schedules, priority checklists, and task breakdowns.",
        color = AgentPlanningColor
    ),
    SUMMARIZING(
        id = "SUMMARIZING",
        title = "Summarizing Agent",
        urduTitle = "خلاصہ اسپیشلسٹ",
        description = "Distilling lengthy articles, chats, and reports into key takeaways.",
        color = AgentSummarizingColor
    ),
    TRANSLATION(
        id = "TRANSLATION",
        title = "Translation Agent",
        urduTitle = "ترجمہ اسپیشلسٹ",
        description = "Accurate Pakistani Roman Urdu, Formal Urdu, and English translation.",
        color = AgentTranslationColor
    ),
    EMAIL(
        id = "EMAIL",
        title = "Email Agent",
        urduTitle = "ای میل اسپیشلسٹ",
        description = "Drafting professional business communications with native send action.",
        color = AgentEmailColor
    ),
    TRADING(
        id = "TRADING",
        title = "Trading Agent",
        urduTitle = "فاریکس و رسک مینجمنٹ",
        description = "Forex lot size calculations, risk-reward ratios, trading journal, and Quotex/Pocket Option analysis.",
        color = AgentTradingColor
    ),
    GENERAL(
        id = "GENERAL",
        title = "General Assistant",
        urduTitle = "عام معاون",
        description = "Everyday Pakistani friendly banter, quick answers, and assistance.",
        color = AgentGeneralColor
    );

    val icon: ImageVector
        get() = when (this) {
            MASTER -> Icons.Default.Psychology
            RESEARCH -> Icons.Default.Search
            WRITING -> Icons.Default.Edit
            CODING -> Icons.Default.Code
            PLANNING -> Icons.Default.EventNote
            SUMMARIZING -> Icons.Default.Summarize
            TRANSLATION -> Icons.Default.Language
            EMAIL -> Icons.Default.Email
            TRADING -> Icons.AutoMirrored.Filled.TrendingUp
            GENERAL -> Icons.Default.Psychology
        }
}
