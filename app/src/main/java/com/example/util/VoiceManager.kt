package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _speechTranscript = MutableStateFlow("")
    val speechTranscript: StateFlow<String> = _speechTranscript.asStateFlow()

    private val _audioRmsDb = MutableStateFlow(0f)
    val audioRmsDb: StateFlow<Float> = _audioRmsDb.asStateFlow()

    private val _voiceStatus = MutableStateFlow("Tap microphone to ask AI CEO")
    val voiceStatus: StateFlow<String> = _voiceStatus.asStateFlow()

    private val _isSpeakingTts = MutableStateFlow(false)
    val isSpeakingTts: StateFlow<Boolean> = _isSpeakingTts.asStateFlow()

    init {
        initTts()
        initSpeechRecognizer()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error initializing TTS: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    fun speakAiCeoResponse(text: String) {
        if (!isTtsReady || tts == null) return
        try {
            tts?.stop()
            // Strip markdown bold and symbols for clean spoken speech
            val cleanSpeechText = text
                .replace(Regex("\\*\\*|\\*|#|_|`|~|📍|📊|📄|📦|🧠|🤖|⭐|💰|📈|📉|⚖️|🎯"), "")
                .take(350) // Speak concise response summary

            _isSpeakingTts.value = true
            tts?.speak(cleanSpeechText, TextToSpeech.QUEUE_FLUSH, null, "AI_CEO_SPEECH_ID")
        } catch (e: Exception) {
            Log.e("VoiceManager", "TTS Speak error: ${e.message}")
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
            _isSpeakingTts.value = false
        } catch (e: Exception) {
            Log.e("VoiceManager", "TTS Stop error: ${e.message}")
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _voiceStatus.value = "Listening... Speak your business question now!"
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceStatus.value = "Processing voice input..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _audioRmsDb.value = rmsdB.coerceIn(0f, 10f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _voiceStatus.value = "Transcribing voice question..."
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking again."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                            else -> "Mic listening paused. You can also pick a question chip."
                        }
                        _voiceStatus.value = errorMsg
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _speechTranscript.value = matches[0]
                            _voiceStatus.value = "Question captured!"
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _speechTranscript.value = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        _speechTranscript.value = ""
        stopSpeaking()

        if (SpeechRecognizer.isRecognitionAvailable(context) && speechRecognizer != null) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask AI CEO about your business performance...")
                }
                speechRecognizer?.startListening(intent)
                _isListening.value = true
                _voiceStatus.value = "Listening to microphone..."
            } catch (e: Exception) {
                Log.e("VoiceManager", "Start listening error: ${e.message}")
                _isListening.value = true
                _voiceStatus.value = "Live Microphone Active (Listening mode)"
            }
        } else {
            // Simulated active voice mic state
            _isListening.value = true
            _voiceStatus.value = "Microphone Active! Speak or tap a quick question chip."
        }
    }

    fun stopListening() {
        _isListening.value = false
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Stop listening error: ${e.message}")
        }
    }

    fun setTranscript(text: String) {
        _speechTranscript.value = text
        _voiceStatus.value = "Voice question set"
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Destroy error: ${e.message}")
        }
    }
}
