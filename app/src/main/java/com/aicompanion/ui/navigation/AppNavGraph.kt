package com.aicompanion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aicompanion.ui.screens.ConversationScreen
import com.aicompanion.ui.screens.HomeScreen
import com.aicompanion.ui.screens.MemoryBrowserScreen
import com.aicompanion.ui.screens.PersonalitySetupScreen
import com.aicompanion.ui.screens.SettingsScreen
import com.aicompanion.ui.screens.VoiceEnrollmentScreen

object Routes {
    const val HOME = "home"
    const val CONVERSATION = "conversation"
    const val PERSONALITY_SETUP = "personality_setup"
    const val VOICE_ENROLLMENT = "voice_enrollment"
    const val MEMORIES = "memories"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.CONVERSATION
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToConversation = {
                    navController.navigate(Routes.CONVERSATION)
                },
                onNavigateToPersonality = {
                    navController.navigate(Routes.PERSONALITY_SETUP)
                },
                onNavigateToMemories = {
                    navController.navigate(Routes.MEMORIES)
                }
            )
        }
        composable(Routes.CONVERSATION) {
            ConversationScreen(
                onNavigateToPersonality = {
                    navController.navigate(Routes.PERSONALITY_SETUP)
                },
                onNavigateToMemories = {
                    navController.navigate(Routes.MEMORIES)
                },
                onNavigateToVoiceEnrollment = {
                    navController.navigate(Routes.VOICE_ENROLLMENT)
                }
            )
        }
        composable(Routes.PERSONALITY_SETUP) {
            PersonalitySetupScreen()
        }
        composable(Routes.VOICE_ENROLLMENT) {
            VoiceEnrollmentScreen()
        }
        composable(Routes.MEMORIES) {
            MemoryBrowserScreen()
        }
        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
    }
}
