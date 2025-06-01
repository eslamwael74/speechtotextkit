package com.eslamwael74.speechtotextcompose

import androidx.compose.runtime.Composable
import com.eslamwael74.speechtotext.SpeechRecognizer

/**
 * Remembers and provides a platform-specific SpeechRecognizer instance.
 *
 * This composable function handles all the platform-specific implementation details
 * for initializing the SpeechRecognizer and makes it available for use in your UI.
 *
 * @return SpeechRecognizer instance initialized for the current platform
 */
@Composable
expect fun rememberSpeechToText(): SpeechRecognizer
