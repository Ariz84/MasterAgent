package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class ContinuousVoiceEngine(
    private val context: Context,
    private val onUserSpeechRecognized: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null
    private var isSpeechRecognizerAvailable = false

    // State flows
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _currentStatusText = MutableStateFlow("Initializing...")
    val currentStatusText: StateFlow<String> = _currentStatusText.asStateFlow()

    private val _latestTranscript = MutableStateFlow("")
    val latestTranscript: StateFlow<String> = _latestTranscript.asStateFlow()

    private var hasAudioPermission = false
    private var shouldKeepListening = true
    private var isEngineDestroyed = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    isSpeechRecognizerAvailable = true
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {
                                _isListening.value = true
                                _currentStatusText.value = "Listening to you (Hands-Free)..."
                            }

                            override fun onBeginningOfSpeech() {
                                _currentStatusText.value = "Voice detected..."
                            }

                            override fun onRmsChanged(rmsdB: Float) {
                                // Normalize rmsdB (typically -2 to 10) to 0.0 .. 1.0 range
                                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                                _audioRms.value = normalized
                            }

                            override fun onBufferReceived(buffer: ByteArray?) {}

                            override fun onEndOfSpeech() {
                                _isListening.value = false
                                _currentStatusText.value = "Processing instruction..."
                            }

                            override fun onError(error: Int) {
                                _isListening.value = false
                                _audioRms.value = 0f
                                Log.d("ContinuousVoiceEngine", "Speech recognizer error: $error")
                                // If no match or timeout, loop back to listen if permitted and not speaking
                                if (shouldKeepListening && !isEngineDestroyed && !_isSpeaking.value && hasAudioPermission) {
                                    mainHandler.postDelayed({
                                        startListeningInternal()
                                    }, 400)
                                }
                            }

                            override fun onResults(results: Bundle?) {
                                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val text = matches?.firstOrNull()?.trim() ?: ""
                                _isListening.value = false
                                _audioRms.value = 0f

                                if (text.isNotBlank()) {
                                    _latestTranscript.value = text
                                    _currentStatusText.value = "Understood: \"$text\""
                                    onUserSpeechRecognized(text)
                                } else {
                                    if (shouldKeepListening && !isEngineDestroyed && !_isSpeaking.value) {
                                        startListeningInternal()
                                    }
                                }
                            }

                            override fun onPartialResults(partialResults: Bundle?) {
                                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                                if (!partial.isNullOrBlank()) {
                                    _latestTranscript.value = partial
                                }
                            }

                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })
                    }
                }
            } catch (e: Exception) {
                Log.e("ContinuousVoiceEngine", "Failed to init speech recognizer", e)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            val urResult = tts?.setLanguage(Locale.forLanguageTag("ur-PK"))
            if (urResult == TextToSpeech.LANG_MISSING_DATA || urResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                val hiResult = tts?.setLanguage(Locale.forLanguageTag("hi-IN"))
                if (hiResult == TextToSpeech.LANG_MISSING_DATA || hiResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.ENGLISH)
                }
            }
            tts?.setSpeechRate(0.95f)
            tts?.setPitch(1.0f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _isListening.value = false
                    _currentStatusText.value = "Master Agent speaking..."
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    // As soon as assistant finishes speaking, activate hands-free listening
                    if (shouldKeepListening && !isEngineDestroyed && hasAudioPermission) {
                        mainHandler.postDelayed({
                            startListeningInternal()
                        }, 250)
                    } else {
                        _currentStatusText.value = "Ready"
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    if (shouldKeepListening && hasAudioPermission) {
                        startListeningInternal()
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    if (shouldKeepListening && hasAudioPermission) {
                        startListeningInternal()
                    }
                }
            })
        }
    }

    fun setAudioPermissionGranted(granted: Boolean) {
        hasAudioPermission = granted
        if (granted && !_isSpeaking.value && !_isListening.value) {
            startListeningInternal()
        }
    }

    fun speak(text: String, onSpeechFinished: (() -> Unit)? = null) {
        if (!isTtsReady || isEngineDestroyed) return

        stopListening()

        val cleanText = text
            .replace(Regex("[#*`_~>\\[\\]()]"), "")
            .replace(Regex("\\bhttps?://\\S+"), "link")
            .trim()

        if (cleanText.isEmpty()) return

        tts?.stop()
        _isSpeaking.value = true
        _currentStatusText.value = "Master Agent speaking..."
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "master_speech_${System.currentTimeMillis()}")
    }

    fun interrupt() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
            _isSpeaking.value = false
        }
        if (hasAudioPermission && !isEngineDestroyed) {
            startListeningInternal()
        }
    }

    private fun startListeningInternal() {
        if (!hasAudioPermission || _isSpeaking.value || isEngineDestroyed) return
        mainHandler.post {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ur-PK")
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }
                speechRecognizer?.startListening(intent)
                _isListening.value = true
                _currentStatusText.value = "Listening (Hands-Free)..."
            } catch (e: Exception) {
                Log.e("ContinuousVoiceEngine", "Error starting listening", e)
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                _isListening.value = false
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun destroy() {
        isEngineDestroyed = true
        shouldKeepListening = false
        tts?.stop()
        tts?.shutdown()
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
