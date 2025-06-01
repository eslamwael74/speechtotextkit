package com.eslamwael74.speechtotext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SpeechRecognizer {
    /**
     * Current state of the speech recognizer
     */
    val state: StateFlow<SpeechRecognizerState>

    /**
     * Recognition results as they become available
     */
    val results: Flow<SpeechRecognitionResult>

    /**
     * Starts speech recognition
     */
    suspend fun startListening()

    /**
     * Stops speech recognition
     */
    suspend fun stopListening()

    /**
     * Checks if speech recognition is available on the current platform
     */
    fun isAvailable(): Boolean

    /**
     * Cleans up resources used by the speech recognizer.
     */
    fun destroy()
}