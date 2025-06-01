package com.eslamwael74.speechtotextkit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eslamwael74.speechtotextcompose.rememberSpeechToText
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    // Initialize speech recognizer
    val speechRecognizer = rememberSpeechToText()

    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Add speech-to-text section
            Text(
                text = "Speech-to-Text Demo",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Speech-to-text component
            SpeechToTextComponent(
                speechRecognizer = speechRecognizer,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Speech recognition results display
            var recognizedText by remember { mutableStateOf("") }

            // Collect speech recognition results
            LaunchedEffect(speechRecognizer) {
                speechRecognizer.results.collect { result ->
                    recognizedText = result.text
                    println("Speech recognition result: $recognizedText, Error: ${result.error}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display the results section
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Recognition Results:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (recognizedText.isNotEmpty()) recognizedText else "Speech recognition results will appear here...",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

