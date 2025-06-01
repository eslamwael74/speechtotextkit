package com.eslamwael74.speechtotext

actual class SpeechRecognizerFactory {

    actual fun createSpeechRecognizer(): SpeechRecognizer {

        return IosSpeechRecognizer()
    }

}