package com.aicompanion

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AiCompanionApp : Application() {
    companion object {
        const val COMPANION_CHANNEL_ID = "ai_companion_channel"
        const val COMPANION_NOTIFICATION_ID = 1001
        const val PROACTIVE_CHANNEL_ID = "ai_companion_proactive"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        // Foreground service channel
        val companionChannel = NotificationChannel(
            COMPANION_CHANNEL_ID,
            "AI Companion",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shown when AI companion is listening"
            setShowBadge(false)
        }
        manager.createNotificationChannel(companionChannel)

        // Proactive notification channel
        val proactiveChannel = NotificationChannel(
            PROACTIVE_CHANNEL_ID,
            "AI Messages",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Proactive messages from your AI companion"
            enableVibration(true)
        }
        manager.createNotificationChannel(proactiveChannel)
    }
}
