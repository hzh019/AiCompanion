package com.aicompanion.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer as AndroidSpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRecognizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var recognizer: AndroidSpeechRecognizer? = null

    fun recognizeDirectly(): Flow<RecognitionResult> = callbackFlow {
        val speechRecognizer = AndroidSpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(RecognitionResult.Ready)
            }

            override fun onBeginningOfSpeech() {
                trySend(RecognitionResult.SpeechStart)
            }

            override fun onRmsChanged(rmsdB: Float) {
                trySend(RecognitionResult.RmsChanged(rmsdB))
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                trySend(RecognitionResult.SpeechEnd)
            }

            override fun onError(error: Int) {
                trySend(RecognitionResult.Error("Recognition error: $error"))
                close()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(AndroidSpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                trySend(RecognitionResult.FinalResult(text))
                close()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(AndroidSpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    trySend(RecognitionResult.PartialResult(text))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)

        awaitClose {
            speechRecognizer.destroy()
        }
    }

    fun cancel() {
        recognizer?.cancel()
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }

    fun isAvailable(): Boolean {
        return AndroidSpeechRecognizer.isRecognitionAvailable(context)
    }
}

sealed class RecognitionResult {
    data object Ready : RecognitionResult()
    data object SpeechStart : RecognitionResult()
    data object SpeechEnd : RecognitionResult()
    data class RmsChanged(val rmsdB: Float) : RecognitionResult()
    data class PartialResult(val text: String) : RecognitionResult()
    data class FinalResult(val text: String) : RecognitionResult()
    data class Error(val message: String) : RecognitionResult()
}
