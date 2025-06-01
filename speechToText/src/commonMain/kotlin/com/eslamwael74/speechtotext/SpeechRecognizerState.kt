package com.eslamwael74.speechtotext

/**
 * Represents the current state of speech recognition
 */
enum class SpeechRecognizerState {
    IDLE,
    LISTENING,
    PROCESSING,
    ERROR
}