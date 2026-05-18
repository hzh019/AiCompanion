package com.aicompanion.conversation

import android.util.Log
import com.aicompanion.audio.AudioCaptureManager
import com.aicompanion.audio.AudioDeviceRouter
import com.aicompanion.audio.AudioFocusManager
import com.aicompanion.audio.AudioPlayer
import com.aicompanion.audio.AudioRoute
import com.aicompanion.data.local.entity.MessageEntity
import com.aicompanion.data.repository.ConversationRepository
import com.aicompanion.data.repository.MemoryRepository
import com.aicompanion.llm.ContextWindowManager
import com.aicompanion.llm.DeepSeekClient
import com.aicompanion.llm.PromptBuilder
import com.aicompanion.memory.MemoryExtractor
import com.aicompanion.personality.EmotionInjector
import com.aicompanion.personality.PersonalityManager
import com.aicompanion.voice.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationManager @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val audioCaptureManager: AudioCaptureManager,
    private val audioPlayer: AudioPlayer,
    private val audioDeviceRouter: AudioDeviceRouter,
    private val audioFocusManager: AudioFocusManager,
    private val voiceActivityDetector: VoiceActivityDetector,
    private val speechRecognizer: SpeechRecognizer,
    private val textToSpeechEngine: TextToSpeechEngine,
    private val deepSeekClient: DeepSeekClient,
    private val promptBuilder: PromptBuilder,
    private val personalityManager: PersonalityManager,
    private val memoryRepository: MemoryRepository,
    private val conversationRepository: ConversationRepository,
    private val contextWindowManager: ContextWindowManager,
    private val emotionInjector: EmotionInjector,
    private val interruptHandler: InterruptHandler,
    private val memoryExtractor: MemoryExtractor
) {
    companion object {
        private const val TAG = "ConversationManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<ConversationState>(ConversationState.Idle)
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _responseText = MutableStateFlow("")
    val responseText: StateFlow<String> = _responseText.asStateFlow()

    private var currentSession: ConversationSession? = null
    private var listeningJob: Job? = null
    private var isAlwaysListening = false

    fun startListening(alwaysListening: Boolean = false) {
        if (_state.value !is ConversationState.Idle) return
        isAlwaysListening = alwaysListening

        // Request audio focus
        audioFocusManager.requestFocus()

        // Route audio based on connected devices
        if (audioDeviceRouter.hasBluetoothSco()) {
            audioDeviceRouter.startBluetoothSco()
        } else {
            audioDeviceRouter.routeToSpeaker()
        }

        // Start audio capture
        val started = audioCaptureManager.startRecording()
        if (!started) {
            _state.value = ConversationState.Error("无法启动麦克风")
            return
        }

        _state.value = ConversationState.Listening
        currentSession = ConversationSession()

        // Begin VAD processing loop
        listeningJob = scope.launch {
            audioCaptureManager.audioFlow.collect { frame ->
                val vadState = voiceActivityDetector.processFrame(frame)

                when (vadState) {
                    VadState.SPEECH_END -> {
                        // Speech detected and ended — process utterance
                        processUtterance()
                    }
                    else -> {
                        // Continue listening
                    }
                }
            }
        }
    }

    private suspend fun processUtterance() {
        _state.value = ConversationState.Processing
        val utteranceFrames = voiceActivityDetector.getUtteranceFrames()

        if (utteranceFrames.isEmpty()) {
            voiceActivityDetector.reset()
            _state.value = ConversationState.Listening
            return
        }

        // TODO Phase 4: Insert Speaker ID verification here
        // val isOwner = speakerIdentifier.verify(utteranceFrames)
        // if (!isOwner) { rejectNonOwner(); return }

        // Convert utterance frames to text via STT
        val userText = recognizeSpeech(utteranceFrames)
        if (userText.isNullOrBlank()) {
            voiceActivityDetector.reset()
            _state.value = if (isAlwaysListening) ConversationState.Listening else ConversationState.Idle
            return
        }

        _partialText.value = userText
        voiceActivityDetector.reset()

        // Save user message
        val session = currentSession!!
        val userMessage = MessageEntity(
            conversationId = session.conversationId,
            role = "user",
            content = userText
        )
        session.addMessage(userMessage)

        // Generate AI response
        _state.value = ConversationState.Thinking
        val responseText = generateResponse(userText)

        if (responseText.isNullOrBlank()) {
            _state.value = ConversationState.Error("生成回复失败")
            return
        }

        _responseText.value = responseText

        // Save assistant message
        val assistantMessage = MessageEntity(
            conversationId = session.conversationId,
            role = "assistant",
            content = responseText
        )
        session.addMessage(assistantMessage)

        // Speak response
        _state.value = ConversationState.Speaking
        speakResponse(responseText)

        // Extract memories in background
        scope.launch {
            try {
                memoryExtractor.extractMemories(userText, responseText, session.conversationId)
            } catch (e: Exception) {
                Log.e(TAG, "Memory extraction failed", e)
            }
        }

        // Save messages to database
        scope.launch {
            try {
                conversationRepository.insertMessage(userMessage)
                conversationRepository.insertMessage(assistantMessage)
                conversationRepository.updateConversation(
                    conversationRepository.getConversationById(session.conversationId)?.copy(
                        messageCount = session.messages.size
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save messages", e)
            }
        }

        // Return to listening or idle
        if (isAlwaysListening) {
            _state.value = ConversationState.Listening
        }
    }

    private suspend fun recognizeSpeech(frames: List<ShortArray>): String? {
        // Convert ShortArray frames to ByteArray
        val byteBuffer = java.nio.ByteBuffer.allocate(frames.sumOf { it.size * 2 })
        frames.forEach { frame ->
            frame.forEach { sample ->
                byteBuffer.putShort(sample)
            }
        }
        val pcmData = byteBuffer.array()

        // Use Android's built-in SpeechRecognizer
        return try {
            recognizeWithAndroidSTT(pcmData)
        } catch (e: Exception) {
            Log.e(TAG, "STT failed", e)
            null
        }
    }

    private suspend fun recognizeWithAndroidSTT(pcmData: ByteArray): String? {
        // For Android built-in SpeechRecognizer, we use the direct flow
        // which opens its own mic. Here we collect the result.
        var result: String? = null
        try {
            speechRecognizer.recognizeDirectly()
                .takeWhile { it !is RecognitionResult.FinalResult && it !is RecognitionResult.Error }
                .collect { recognitionResult ->
                    when (recognitionResult) {
                        is RecognitionResult.PartialResult -> {
                            _partialText.value = recognitionResult.text
                        }
                        is RecognitionResult.FinalResult -> {
                            result = recognitionResult.text
                        }
                        is RecognitionResult.Error -> {
                            Log.e(TAG, recognitionResult.message)
                        }
                        else -> {}
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Android STT failed", e)
        }
        return result
    }

    private suspend fun generateResponse(userText: String): String? {
        return try {
            val personality = personalityManager.getActivePersonality()
            if (personality == null) {
                Log.e(TAG, "No active personality")
                return "请先设置AI人格"
            }

            val memories = memoryRepository.getTopMemories(5)
            val systemPrompt = promptBuilder.buildSystemPrompt(personality, memories)

            // Get recent context
            val recentMessages = currentSession?.getRecentMessages(20) ?: emptyList()
            val trimmedMessages = contextWindowManager.trimMessages(recentMessages)

            val chatMessages = promptBuilder.buildMessages(trimmedMessages, userText)

            // Stream LLM response
            val responseBuilder = StringBuilder()
            deepSeekClient.chatCompletionStream(systemPrompt, chatMessages)
                .collect { token ->
                    responseBuilder.append(token)
                    _responseText.value = responseBuilder.toString()
                }

            responseBuilder.toString().ifBlank { "嗯，我在听呢..." }
        } catch (e: Exception) {
            Log.e(TAG, "LLM generation failed", e)
            "抱歉，我暂时无法回复，请稍后再试。"
        }
    }

    private fun speakResponse(text: String) {
        // Set up interrupt monitoring during TTS
        interruptHandler.startMonitoring {
            textToSpeechEngine.stop()
            _state.value = ConversationState.Idle
        }

        audioPlayer.setOnPlaybackComplete {
            interruptHandler.stopMonitoring()
            _state.value = if (isAlwaysListening) ConversationState.Listening else ConversationState.Idle
        }

        // Use Android TTS for speaking
        textToSpeechEngine.initialize { success ->
            if (success) {
                textToSpeechEngine.speak(text)
            }
        }

        // Monitor TTS events
        scope.launch {
            textToSpeechEngine.getEvents().collect { event ->
                when (event) {
                    is TtsEvent.Done -> {
                        interruptHandler.stopMonitoring()
                        _state.value = if (isAlwaysListening) ConversationState.Listening else ConversationState.Idle
                    }
                    is TtsEvent.Interrupted -> {
                        _state.value = ConversationState.Idle
                    }
                    is TtsEvent.Error -> {
                        Log.e(TAG, event.message)
                    }
                }
            }
        }
    }

    fun stopListening() {
        isAlwaysListening = false
        listeningJob?.cancel()
        listeningJob = null
        audioCaptureManager.stopRecording()
        audioFocusManager.abandonFocus()
        audioDeviceRouter.stopBluetoothSco()
        _state.value = ConversationState.Idle
    }

    fun interrupt() {
        textToSpeechEngine.stop()
        audioPlayer.stop()
        _state.value = if (isAlwaysListening) ConversationState.Listening else ConversationState.Idle
    }

    fun toggleAlwaysListening() {
        if (isAlwaysListening) {
            stopListening()
        } else {
            startListening(alwaysListening = true)
        }
    }

    fun getCurrentSession(): ConversationSession? = currentSession

    fun destroy() {
        stopListening()
        textToSpeechEngine.shutdown()
        speechRecognizer.destroy()
        scope.cancel()
    }
}
