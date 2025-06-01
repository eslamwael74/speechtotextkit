package com.eslamwael74.speechtotextkit.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eslamwael74.speechtotext.SpeechRecognitionResult
import com.eslamwael74.speechtotext.SpeechRecognizerState
import com.eslamwael74.speechtotextcompose.rememberSpeechToText
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun SpeechRecognitionExample() {
    val speechRecognizer = rememberSpeechToText()
    val scope = rememberCoroutineScope()

    var recognitionText by remember { mutableStateOf("") }
    var currentState by remember { mutableStateOf<SpeechRecognizerState>(SpeechRecognizerState.IDLE) }
    var isAvailable by remember { mutableStateOf(false) }

    // Check if speech recognition is available
    LaunchedEffect(speechRecognizer) {
        isAvailable = speechRecognizer.isAvailable()
    }

    // Collect recognizer state
    LaunchedEffect(speechRecognizer) {
        speechRecognizer.state.collect { state ->
            currentState = state
        }
    }

    // Collect speech recognition results
    LaunchedEffect(speechRecognizer) {
        speechRecognizer.results.onEach { result ->
            // Update with the latest recognition result
            recognitionText = result.text
        }.collect()
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Speech Recognition",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isAvailable) {
            Text(
                text = "Speech recognition is not available on this device",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            // Status card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Status: ${currentState.toDisplayString()}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Recognition Result:",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = recognitionText.ifEmpty { "No text recognized yet" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (recognitionText.isEmpty()) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control buttons
            Button(
                onClick = {
                    scope.launch {
                        if (currentState != SpeechRecognizerState.LISTENING) {
                            speechRecognizer.startListening()
                        } else {
                            speechRecognizer.stopListening()
                        }
                    }
                }
            ) {
                Text(
                    text = if (currentState == SpeechRecognizerState.LISTENING) {
                        "Stop Listening"
                    } else {
                        "Start Listening"
                    }
                )
            }
        }
    }
}

// Helper function to convert SpeechRecognizerState to a display string
private fun SpeechRecognizerState.toDisplayString(): String {
    return when (this) {
        SpeechRecognizerState.IDLE -> "Idle"
        SpeechRecognizerState.LISTENING -> "Listening..."
        SpeechRecognizerState.PROCESSING -> "Processing..."
        SpeechRecognizerState.ERROR -> "Error."
    }
}
