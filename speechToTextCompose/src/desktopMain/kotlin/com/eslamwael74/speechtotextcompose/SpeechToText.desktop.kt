package com.eslamwael74.speechtotextcompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.eslamwael74.speechtotext.SpeechRecognizer
import com.eslamwael74.speechtotext.SpeechRecognizerFactory

/**
 * Desktop implementation of rememberSpeechToText.
 *
 * @return SpeechRecognizer instance for Desktop
 */
@Composable
actual fun rememberSpeechToText(): SpeechRecognizer {
    val factory = remember { SpeechRecognizerFactory() }
    return remember { factory.createSpeechRecognizer() }
}
