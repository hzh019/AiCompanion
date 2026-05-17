package com.aicompanion.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private val utteranceChannel = Channel<TtsEvent>(Channel.BUFFERED)
    private var isInitialized = false
    private var initCallback: ((Boolean) -> Unit)? = null

    fun initialize(callback: (Boolean) -> Unit) {
        if (isInitialized) {
            callback(true)
            return
        }

        initCallback = callback
        tts = TextToSpeech(context) { status ->
            isInitialized = (status == TextToSpeech.SUCCESS)
            if (isInitialized) {
                tts?.language = Locale.CHINESE
                // Try to get a higher quality voice
                tts?.voice = tts?.voices?.firstOrNull {
                    it.name.contains("zh", ignoreCase = true) &&
                    it.quality >= Voice.QUALITY_HIGH
                }
            }
            initCallback?.invoke(isInitialized)
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                utteranceChannel.trySend(TtsEvent.Done)
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                utteranceChannel.trySend(TtsEvent.Error("TTS playback error"))
            }
            override fun onError(utteranceId: String?, errorCode: Int) {
                utteranceChannel.trySend(TtsEvent.Error("TTS error code: $errorCode"))
            }
            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                if (interrupted) {
                    utteranceChannel.trySend(TtsEvent.Interrupted)
                }
            }
        })
    }

    fun speak(text: String, utteranceId: String = System.currentTimeMillis().toString()) {
        if (!isInitialized) return

        // Split long text into manageable chunks
        val chunks = splitIntoSentences(text)
        chunks.forEachIndexed { index, chunk ->
            tts?.speak(chunk, TextToSpeech.QUEUE_ADD, Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "${utteranceId}_$index")
            }, "${utteranceId}_$index")
        }
    }

    fun getEvents(): Flow<TtsEvent> = utteranceChannel.receiveAsFlow()

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true

    fun setPitch(pitch: Float) {
        tts?.setPitch(maxOf(0.5f, minOf(2.0f, pitch)))
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(maxOf(0.5f, minOf(2.0f, rate)))
    }

    private fun splitIntoSentences(text: String): List<String> {
        // Split by Chinese/English punctuation
        val sentences = text.split(Regex("(?<=[。！？.!?，,；;：:\n])"))
        return sentences.filter { it.isNotBlank() }
    }
}

sealed class TtsEvent {
    data object Done : TtsEvent()
    data object Interrupted : TtsEvent()
    data class Error(val message: String) : TtsEvent()
}
