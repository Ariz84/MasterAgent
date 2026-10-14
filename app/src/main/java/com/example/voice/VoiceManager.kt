package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isVoiceOutputEnabled = MutableStateFlow(true)
    val isVoiceOutputEnabled: StateFlow<Boolean> = _isVoiceOutputEnabled.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            // Try Urdu PK, fallback to Hindi IN or English IN/US for natural phonetic Roman Urdu pronunciation
            val result = tts?.setLanguage(Locale.forLanguageTag("ur-PK"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
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
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    Log.w("VoiceManager", "TTS error code: $errorCode")
                }
            })
        } else {
            Log.e("VoiceManager", "TTS initialization failed: $status")
        }
    }

    /**
     * Speaks the assistant's response. Automatically cleans markdown formatting like * or # for smooth speech.
     */
    fun speak(text: String, utteranceId: String = "master_agent_resp_${System.currentTimeMillis()}") {
        if (!_isVoiceOutputEnabled.value) return
        if (!isInitialized) return

        // Clean out markdown symbols for natural speech
        val cleanText = text
            .replace(Regex("[#*`_~>\\[\\]()]"), "")
            .replace(Regex("\\bhttps?://\\S+"), "link")
            .trim()

        if (cleanText.isEmpty()) return

        tts?.stop()
        _isSpeaking.value = true
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Instantly stops speech (supports natural user interruption)
     */
    fun interrupt() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun setVoiceOutputEnabled(enabled: Boolean) {
        _isVoiceOutputEnabled.value = enabled
        if (!enabled) {
            interrupt()
        }
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
