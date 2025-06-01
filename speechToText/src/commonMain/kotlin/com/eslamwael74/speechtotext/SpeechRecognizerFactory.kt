package com.eslamwael74.speechtotext

/**
 * Factory to create instances of [SpeechRecognizer].
 */
expect class SpeechRecognizerFactory {

    fun createSpeechRecognizer(): SpeechRecognizer

}