package com.eslamwael74.speechtotextcompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.eslamwael74.speechtotext.SpeechRecognizer
import com.eslamwael74.speechtotext.SpeechRecognizerFactory

/**
 * Android implementation of rememberSpeechToText that uses Android Context.
 *
 * @return SpeechRecognizer instance for Android
 */
@Composable
actual fun rememberSpeechToText(): SpeechRecognizer {
    val context = LocalContext.current
    val factory = remember { SpeechRecognizerFactory(context) }
    return remember { factory.createSpeechRecognizer() }
}
