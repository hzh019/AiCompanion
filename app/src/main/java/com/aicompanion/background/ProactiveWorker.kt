package com.aicompanion.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.aicompanion.data.local.entity.ProactiveTriggerEntity
import com.aicompanion.data.repository.PersonalityRepository
import com.aicompanion.data.repository.ProactiveTriggerRepository
import com.aicompanion.llm.DeepSeekClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

@HiltWorker
class ProactiveWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val triggerRepository: ProactiveTriggerRepository,
    private val personalityRepository: PersonalityRepository,
    private val deepSeekClient: DeepSeekClient,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "ProactiveWorker"
        private const val UNIQUE_WORK_NAME = "proactive_check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ProactiveWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Proactive check running...")

        return try {
            val triggers = triggerRepository.getAllEnabled().firstOrNull() ?: return Result.success()

            for (trigger in triggers) {
                if (shouldTrigger(trigger)) {
                    generateAndSendProactiveMessage(trigger)
                    triggerRepository.update(
                        trigger.copy(lastTriggeredAt = System.currentTimeMillis())
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Proactive check failed", e)
            Result.retry()
        }
    }

    private fun shouldTrigger(trigger: ProactiveTriggerEntity): Boolean {
        val now = System.currentTimeMillis()

        // Check cooldown
        if (trigger.lastTriggeredAt != null) {
            val timeSinceLastTrigger = now - trigger.lastTriggeredAt
            if (timeSinceLastTrigger < trigger.cooldownMinutes * 60 * 1000L) {
                return false
            }
        }

        // Check trigger type conditions
        val calendar = java.util.Calendar.getInstance()
        return when (trigger.triggerType) {
            "time_of_day" -> {
                try {
                    val config = org.json.JSONObject(trigger.configJson)
                    val targetHour = config.optInt("hour", -1)
                    val targetMinute = config.optInt("minute", 0)
                    val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                    val currentMinute = calendar.get(java.util.Calendar.MINUTE)

                    // Allow ~15 minute window
                    currentHour == targetHour &&
                    kotlin.math.abs(currentMinute - targetMinute) <= 15
                } catch (e: Exception) {
                    false
                }
            }
            "inactivity" -> {
                try {
                    val config = org.json.JSONObject(trigger.configJson)
                    val inactivityMinutes = config.optInt("inactivity_minutes", 120)
                    // For now, just check cooldown (simplified)
                    trigger.lastTriggeredAt == null || true
                } catch (e: Exception) {
                    false
                }
            }
            "interval" -> true // Always trigger if cooldown passed
            else -> false
        }
    }

    private suspend fun generateAndSendProactiveMessage(trigger: ProactiveTriggerEntity) {
        try {
            val personality = personalityRepository.getActive() ?: return

            val systemPrompt = """
你是${personality.characterName}，用户的${personality.relationship}。
性格：${personality.personalityTraits}
说话风格：${personality.speakingStyle}

现在你需要主动给用户发一条消息。${trigger.promptTemplate}

要求：
- 只用1-2句话
- 语气自然，像真人
- 不要重复"我是AI"之类的话
- 直接说内容即可
            """.trimIndent()

            val messages = listOf(
                mapOf("role" to "user", "content" to "请给我发一条消息")
            )

            val responseBuilder = StringBuilder()
            deepSeekClient.chatCompletionStream(
                systemPrompt = systemPrompt,
                messages = messages
            ).collect { token ->
                responseBuilder.append(token)
            }

            val message = responseBuilder.toString().trim()
            if (message.isNotBlank()) {
                notificationHelper.showProactiveNotification(
                    title = trigger.notificationTitle.ifBlank { "${personality.characterName}发来消息" },
                    body = message.ifBlank { "在想你..." }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate proactive message", e)
        }
    }
}
