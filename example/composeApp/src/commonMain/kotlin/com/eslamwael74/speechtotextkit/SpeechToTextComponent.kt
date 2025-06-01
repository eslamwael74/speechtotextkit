package com.eslamwael74.speechtotextkit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eslamwael74.speechtotext.SpeechRecognizer
import com.eslamwael74.speechtotext.SpeechRecognizerState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A composable UI component that provides speech-to-text functionality
 */
@Composable
fun SpeechToTextComponent(
    speechRecognizer: SpeechRecognizer,
    modifier: Modifier = Modifier
) {
    val recognizerState by speechRecognizer.state.collectAsState()
    var recognizedText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Collect speech recognition results
    LaunchedEffect(speechRecognizer) {
        speechRecognizer.results.collectLatest { result ->
            recognizedText = result.text
            errorMessage = result.error
            println("Speech recognition result: $recognizedText, Error: $errorMessage")
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (recognizerState) {
                    SpeechRecognizerState.IDLE -> "Ready for speech input"
                    SpeechRecognizerState.LISTENING -> "Listening..."
                    SpeechRecognizerState.PROCESSING -> "Processing speech..."
                    SpeechRecognizerState.ERROR -> "Error: $errorMessage"
                }
            )

            Text(
                text = recognizedText,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Button(
                onClick = {
                    if (recognizerState == SpeechRecognizerState.LISTENING) {
                        // If already listening, stop
                        MainScope().launch {
                            speechRecognizer.stopListening()
                        }
                    } else {
                        // Start listening
                        recognizedText = ""
                        errorMessage = null
                        MainScope().launch {
                            speechRecognizer.startListening()
                        }
                    }
                },
                enabled = recognizerState != SpeechRecognizerState.PROCESSING
            ) {
                Text(
                    text = when (recognizerState) {
                        SpeechRecognizerState.LISTENING -> "Stop"
                        else -> "Start Speech Recognition"
                    }
                )
            }
        }
    }
}