package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.MasterAgentOrchestrator
import com.example.communication.CommunicationManager
import com.example.data.local.MasterAgentDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.TradeJournalEntity
import com.example.model.SpecialistAgentType
import com.example.network.GeminiClient
import com.example.network.TermuxConnector
import com.example.trading.ForexRiskResult
import com.example.trading.TradingEngine
import com.example.voice.ContinuousVoiceEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    CONVERSATION, // Voice-First 3D Robot Orb Avatar Screen
    SPECIALIST_INSPECTOR,
    TRADING_TOOLS,
    TERMUX_COMPANION,
    COMMUNICATIONS_STATUS
}

data class ActiveActionCard(
    val type: String, // "WHATSAPP", "SMS", "EMAIL", "TRADING_CALC", "CODE_SNIPPET"
    val payload: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MasterAgentDatabase.getDatabase(application)
    private val chatDao = db.chatDao()
    private val tradeJournalDao = db.tradeJournalDao()

    val geminiClient = GeminiClient()
    val termuxConnector = TermuxConnector(application)

    // Continuous hands-free two-way voice engine
    val voiceEngine = ContinuousVoiceEngine(application) { recognizedSpeech ->
        // Triggered automatically when user speaks into microphone
        onUserSpoke(recognizedSpeech)
    }

    private val orchestrator = MasterAgentOrchestrator(geminiClient, termuxConnector)

    val messages: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trades: StateFlow<List<TradeJournalEntity>> = tradeJournalDao.getAllTrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(AppScreen.CONVERSATION)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentSpecialist = MutableStateFlow(SpecialistAgentType.MASTER)
    val currentSpecialist: StateFlow<SpecialistAgentType> = _currentSpecialist.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _latestAssistantSpeech = MutableStateFlow("Assalam-o-alaikum bhai! Aaj kya kaam karna hai? Main aapki kya madad kar sakta hoon?")
    val latestAssistantSpeech: StateFlow<String> = _latestAssistantSpeech.asStateFlow()

    private val _latestUserSpeech = MutableStateFlow("")
    val latestUserSpeech: StateFlow<String> = _latestUserSpeech.asStateFlow()

    private val _activeAction = MutableStateFlow<ActiveActionCard?>(null)
    val activeAction: StateFlow<ActiveActionCard?> = _activeAction.asStateFlow()

    private val _termuxPingMessage = MutableStateFlow<String?>(null)
    val termuxPingMessage: StateFlow<String?> = _termuxPingMessage.asStateFlow()

    private val _forexResult = MutableStateFlow<ForexRiskResult?>(null)
    val forexResult: StateFlow<ForexRiskResult?> = _forexResult.asStateFlow()

    init {
        startProactiveVoiceSession()
    }

    private fun startProactiveVoiceSession() {
        viewModelScope.launch {
            val greeting = orchestrator.getProactiveGreeting()
            _latestAssistantSpeech.value = greeting

            // Save greeting to history
            val count = chatDao.getMessageCount()
            if (count == 0) {
                chatDao.insertMessage(
                    ChatMessageEntity(
                        isUser = false,
                        text = greeting,
                        agentType = SpecialistAgentType.MASTER.id,
                        agentName = SpecialistAgentType.MASTER.title,
                        statusTag = "PROACTIVE_GREETING"
                    )
                )
            }

            // Immediately speak out greeting in Pakistani Roman Urdu on launch
            voiceEngine.speak(greeting)
        }
    }

    fun onUserSpoke(text: String) {
        val query = text.trim()
        if (query.isEmpty() || _isProcessing.value) return

        _latestUserSpeech.value = query
        _isProcessing.value = true

        viewModelScope.launch {
            // Save user turn
            chatDao.insertMessage(
                ChatMessageEntity(
                    isUser = true,
                    text = query,
                    agentType = "USER",
                    agentName = "Aap (User)"
                )
            )

            // Orchestrate with Master Agent and specialists
            val result = orchestrator.processUserInstruction(query)
            _currentSpecialist.value = result.delegatedAgent
            _latestAssistantSpeech.value = result.masterResponseText

            // Handle action card if present
            if (!result.actionType.isNullOrBlank()) {
                _activeAction.value = ActiveActionCard(result.actionType, result.actionPayload ?: result.masterResponseText)
            } else {
                _activeAction.value = null
            }

            chatDao.insertMessage(
                ChatMessageEntity(
                    isUser = false,
                    text = result.masterResponseText,
                    agentType = result.delegatedAgent.id,
                    agentName = result.delegatedAgent.title,
                    statusTag = result.statusTag,
                    actionType = result.actionType,
                    actionPayload = result.actionPayload
                )
            )

            _isProcessing.value = false

            // Assistant speaks its replies, and automatically resumes listening upon completion
            voiceEngine.speak(result.masterResponseText)
        }
    }

    fun onOrbTapped() {
        // Natural interruption: if assistant is speaking, interrupt; if idle, start listening
        if (voiceEngine.isSpeaking.value) {
            voiceEngine.interrupt()
        }
    }

    fun dismissActionCard() {
        _activeAction.value = null
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setAudioPermissionGranted(granted: Boolean) {
        voiceEngine.setAudioPermissionGranted(granted)
    }

    fun testTermux() {
        viewModelScope.launch {
            _termuxPingMessage.value = "Connecting to Termux localhost..."
            val result = termuxConnector.pingHealth()
            _termuxPingMessage.value = if (result.isSuccess) {
                result.getOrThrow()
            } else {
                result.exceptionOrNull()?.message ?: "Termux connection failed."
            }
        }
    }

    fun updateTermuxConfig(url: String, token: String, enabled: Boolean) {
        termuxConnector.baseUrl = url
        termuxConnector.authToken = token
        termuxConnector.isEnabled = enabled
    }

    fun calculateForex(
        pair: String,
        balance: Double,
        riskPercent: Double,
        entryPrice: Double,
        stopLossPrice: Double,
        takeProfitPrice: Double
    ) {
        val res = TradingEngine.calculateForexRisk(
            pair = pair,
            balance = balance,
            riskPercent = riskPercent,
            entryPrice = entryPrice,
            stopLossPrice = stopLossPrice,
            takeProfitPrice = takeProfitPrice
        )
        _forexResult.value = res
    }

    fun logTrade(
        pair: String,
        direction: String,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit: Double,
        lotSize: Double,
        riskAmountUsd: Double,
        status: String,
        platform: String,
        strategyNotes: String
    ) {
        viewModelScope.launch {
            val trade = TradeJournalEntity(
                pair = pair,
                direction = direction,
                entryPrice = entryPrice,
                stopLoss = stopLoss,
                takeProfit = takeProfit,
                lotSize = lotSize,
                riskAmountUsd = riskAmountUsd,
                status = status,
                platform = platform,
                strategyNotes = strategyNotes
            )
            tradeJournalDao.insertTrade(trade)
        }
    }

    fun deleteTrade(trade: TradeJournalEntity) {
        viewModelScope.launch {
            tradeJournalDao.deleteTrade(trade)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
    }
}
