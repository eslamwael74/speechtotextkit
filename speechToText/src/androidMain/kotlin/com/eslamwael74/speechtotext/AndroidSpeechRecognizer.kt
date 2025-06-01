package com.eslamwael74.speechtotext

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer as AndroidSR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

internal class AndroidSpeechRecognizer(private val context: Context) : SpeechRecognizer {

    private val _state = MutableStateFlow(SpeechRecognizerState.IDLE)
    override val state: StateFlow<SpeechRecognizerState> = _state.asStateFlow()

    // Update to include replay and extraBufferCapacity for reliable emission
    private val _results = MutableSharedFlow<SpeechRecognitionResult>(
        replay = 1,
        extraBufferCapacity = 10
    )
    override val results: Flow<SpeechRecognitionResult> = _results.asSharedFlow()

    private var speechRecognizer: AndroidSR? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override suspend fun startListening() {
        if (isAvailable()) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = AndroidSR.createSpeechRecognizer(context)
                    speechRecognizer?.setRecognitionListener(createRecognitionListener())
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                _state.value = SpeechRecognizerState.LISTENING
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _state.value = SpeechRecognizerState.ERROR
                coroutineScope.launch {
                    _results.emit(SpeechRecognitionResult("", false, e.message))
                }
            }
        } else {
            _state.value = SpeechRecognizerState.ERROR
            coroutineScope.launch {
                _results.emit(
                    SpeechRecognitionResult(
                        "",
                        false,
                        "Speech recognition not available"
                    )
                )
            }
        }
    }

    override suspend fun stopListening() {
        speechRecognizer?.stopListening()
        _state.value = SpeechRecognizerState.IDLE
    }

    override fun isAvailable(): Boolean {
        return AndroidSR.isRecognitionAvailable(context)
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _state.value = SpeechRecognizerState.LISTENING
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _state.value = SpeechRecognizerState.PROCESSING
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    AndroidSR.ERROR_AUDIO -> "Audio recording error"
                    AndroidSR.ERROR_CLIENT -> "Client side error"
                    AndroidSR.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    AndroidSR.ERROR_NETWORK -> "Network error"
                    AndroidSR.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    AndroidSR.ERROR_NO_MATCH -> "No match found"
                    AndroidSR.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                    AndroidSR.ERROR_SERVER -> "Server error"
                    AndroidSR.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }

                _state.value = SpeechRecognizerState.ERROR
                coroutineScope.launch {
                    _results.emit(SpeechRecognitionResult("", false, errorMsg))
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(AndroidSR.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    coroutineScope.launch {
                        _results.emit(SpeechRecognitionResult(matches[0], true))
                        println("Speech recognized and emitted: ${matches[0]}")
                    }
                }
                _state.value = SpeechRecognizerState.IDLE
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(AndroidSR.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    coroutineScope.launch {
                        _results.emit(SpeechRecognitionResult(matches[0], false))
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    override fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        coroutineScope.cancel() // Cancel the coroutine scope to avoid leaks
    }
}