package com.eslamwael74.speechtotext

/**
 * Container for speech recognition results
 */
data class SpeechRecognitionResult(
    val text: String,
    val isFinal: Boolean = false,
    val error: String? = null
)