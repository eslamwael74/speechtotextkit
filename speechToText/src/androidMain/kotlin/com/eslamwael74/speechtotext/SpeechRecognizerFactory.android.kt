package com.eslamwael74.speechtotext

import android.content.Context

actual class SpeechRecognizerFactory(private val context: Context) {

    actual fun createSpeechRecognizer(): SpeechRecognizer {
        val currentAppContext = context

        require(currentAppContext != null) {
            "SpeechRecognizerFactory not initialized. Call SpeechRecognizerFactory.initialize(context) in your Application class."
        }

        return AndroidSpeechRecognizer(currentAppContext)
    }
}