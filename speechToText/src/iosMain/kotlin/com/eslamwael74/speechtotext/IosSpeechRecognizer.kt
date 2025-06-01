package com.eslamwael74.speechtotext

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDuckOthers
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.setActive
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import platform.Speech.SFSpeechRecognizerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.resume

internal class IosSpeechRecognizer : SpeechRecognizer {
    private val _state = MutableStateFlow(SpeechRecognizerState.IDLE)
    override val state: StateFlow<SpeechRecognizerState> = _state.asStateFlow()

    private val _results = MutableSharedFlow<SpeechRecognitionResult>(
        replay = 1, extraBufferCapacity = 10
    )
    override val results: Flow<SpeechRecognitionResult> = _results.asSharedFlow()

    private val audioEngine = AVAudioEngine()
    private val speechRecognizer = SFSpeechRecognizer(locale = NSLocale.currentLocale())
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest? = null
    private var recognitionTask: SFSpeechRecognitionTask? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Delegate for speech recognizer availability changes
    private val speechRecognizerDelegate = object : NSObject(), SFSpeechRecognizerDelegateProtocol {
        override fun speechRecognizer(
            speechRecognizer: SFSpeechRecognizer,
            availabilityDidChange: Boolean
        ) {
            if (!availabilityDidChange) {
                _state.value = SpeechRecognizerState.ERROR
                scope.launch {
                    _results.emit(
                        SpeechRecognitionResult(
                            "",
                            false,
                            "Speech recognizer became unavailable"
                        )
                    )
                }
                if (_state.value == SpeechRecognizerState.LISTENING) {
                    scope.launch { stopListening() }
                }
            }
        }
    }

    init {
        speechRecognizer.delegate = speechRecognizerDelegate
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun startListening() {
        // First ensure permissions are granted
        val permissionsGranted = requestPermissions()
        if (!permissionsGranted) {
            _state.value = SpeechRecognizerState.ERROR
            scope.launch {
                _results.emit(
                    SpeechRecognitionResult(
                        "",
                        false,
                        "Speech recognition permissions denied."
                    )
                )
            }
            return
        }

        // Check if service is available
        if (!isAvailable()) {
            _state.value = SpeechRecognizerState.ERROR
            scope.launch {
                _results.emit(
                    SpeechRecognitionResult(
                        "",
                        false,
                        "Service not available. Speech recognition service unavailable."
                    )
                )
            }
            return
        }

        // Already listening - avoid duplicate sessions
        if (_state.value == SpeechRecognizerState.LISTENING) return

        _state.value = SpeechRecognizerState.LISTENING

        try {
            // Create and configure the recognition request
            val currentRecognitionRequest = SFSpeechAudioBufferRecognitionRequest()
            recognitionRequest = currentRecognitionRequest
            currentRecognitionRequest.shouldReportPartialResults = true

            // Configure audio session
            configureAudioSession()

            // Set up audio input
            val inputNode = audioEngine.inputNode
            val recordingFormat = inputNode.outputFormatForBus(0u)

            // Check for valid format
            if (recordingFormat.channelCount == 0u) {
                _state.value = SpeechRecognizerState.ERROR
                scope.launch {
                    _results.emit(
                        SpeechRecognitionResult(
                            "",
                            false,
                            "Input node has no output format or zero channels."
                        )
                    )
                }
                cleanup()
                return
            }

            // Install tap and begin capturing audio
            inputNode.installTapOnBus(
                0u,
                bufferSize = 1024u,
                format = recordingFormat
            ) { buffer, _ ->
                buffer?.let { currentRecognitionRequest.appendAudioPCMBuffer(it) }
            }

            // Prepare and start audio engine
            audioEngine.prepare()
            audioEngine.startAndReturnError(null)

            // Start recognition task
            recognitionTask =
                speechRecognizer.recognitionTaskWithRequest(currentRecognitionRequest) { result, error ->
                    scope.launch {
                        var isFinal = false
                        if (result != null) {
                            val text = result.bestTranscription.formattedString
                            isFinal = result.isFinal()
                            _results.emit(SpeechRecognitionResult(text, isFinal, null))
                        }

                        if (error != null || isFinal) {
                            if (error != null) {
                                _state.value = SpeechRecognizerState.ERROR
                                _results.emit(
                                    SpeechRecognitionResult(
                                        "",
                                        false,
                                        "Recognition task error: ${error.localizedDescription}"
                                    )
                                )
                            }
                            cleanup()
                        }
                    }
                }
        } catch (e: Exception) {
            _state.value = SpeechRecognizerState.ERROR
            scope.launch {
                _results.emit(
                    SpeechRecognitionResult(
                        "",
                        false,
                        "AudioEngine start/setup error: ${e.message}"
                    )
                )
            }
            cleanup()
        }
    }

    override suspend fun stopListening() {
        recognitionRequest?.endAudio()
        cleanup()
    }

    override fun isAvailable(): Boolean {
        val speechAuthStatus = SFSpeechRecognizer.authorizationStatus()

        val hasSpeechPermission =
            speechAuthStatus == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized
        val hasMicPermission = hasMicrophonePermission()

        return hasSpeechPermission && hasMicPermission && speechRecognizer.isAvailable()
    }

    @OptIn(ExperimentalForeignApi::class) // If not already at class level
    private fun hasMicrophonePermission(): Boolean {
        // micAuthStatus will be of type AVAudioSessionRecordPermission (which is a typealias for NSUInteger)
        val micAuthStatus = AVAudioSession.sharedInstance().recordPermission()
        return micAuthStatus == AVAudioSessionRecordPermissionGranted // Corrected: Direct constant usage
    }

    suspend fun requestPermissions(): Boolean {
        return if (isAvailable()) {
            true
        } else {
            requestSpeechPermissions()
        }
    }

    private suspend fun requestSpeechPermissions(): Boolean =
        suspendCancellableCoroutine { cont ->
            SFSpeechRecognizer.requestAuthorization { status ->
                if (status == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized) {
                    AVAudioSession.sharedInstance()
                        .requestRecordPermission { granted ->
                            cont.resume(granted)
                        }
                } else {
                    cont.resume(false)
                }
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    private fun configureAudioSession() {
        try {
            val audioSession = AVAudioSession.sharedInstance()
            // Correct usage: calling instance methods
            audioSession.setCategory(
                AVAudioSessionCategoryRecord,
                mode = AVAudioSessionModeMeasurement,
                options = AVAudioSessionCategoryOptionDuckOthers,
                error = null
            )
            audioSession.setActive(
                true,
                error = null
            ) // Pass error = null (or byref NSError variable)

            // ... rest of the audio setup ...
        } catch (e: Exception) {
            // ... error handling ...
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun cleanup() {
        if (audioEngine.running) {
            audioEngine.stop()
            audioEngine.inputNode.removeTapOnBus(0u)
        }

        recognitionRequest = null
        recognitionTask?.cancel()
        recognitionTask = null

        // Deactivate audio session
        try {
            AVAudioSession.sharedInstance().setActive(false, null)
        } catch (e: Exception) {
            // Log error but continue cleanup
        }

        _state.value = SpeechRecognizerState.IDLE
    }

    override fun destroy() {
        cleanup()

        speechRecognizer.delegate = null // Remove delegate to avoid memory leaks
        recognitionRequest = null
        recognitionTask = null
        _state.value = SpeechRecognizerState.IDLE
    }

}
