package com.aicompanion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicompanion.ui.screens.ConversationScreen
import com.aicompanion.ui.screens.HomeScreen
import com.aicompanion.ui.screens.MemoryBrowserScreen
import com.aicompanion.ui.screens.PersonalitySetupScreen
import com.aicompanion.ui.screens.SettingsScreen
import com.aicompanion.ui.screens.VoiceEnrollmentScreen
import com.aicompanion.ui.viewmodel.MemoryViewModel
import com.aicompanion.ui.viewmodel.PersonalityViewModel

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
                onNavigateToConversation = { navController.navigate(Routes.CONVERSATION) },
                onNavigateToPersonality = { navController.navigate(Routes.PERSONALITY_SETUP) },
                onNavigateToMemories = { navController.navigate(Routes.MEMORIES) }
            )
        }
        composable(Routes.CONVERSATION) {
            ConversationScreen(
                onNavigateToPersonality = { navController.navigate(Routes.PERSONALITY_SETUP) },
                onNavigateToMemories = { navController.navigate(Routes.MEMORIES) },
                onNavigateToVoiceEnrollment = { navController.navigate(Routes.VOICE_ENROLLMENT) }
            )
        }
        composable(Routes.PERSONALITY_SETUP) {
            val vm: PersonalityViewModel = hiltViewModel()
            PersonalitySetupScreen(
                personalities = vm.personalities,
                activePersonalityId = vm.activeId,
                onSelectPersonality = { vm.setActive(it) },
                onCreateFromTemplate = { vm.createFromTemplate(it) },
                onDeletePersonality = { vm.delete(it) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.VOICE_ENROLLMENT) {
            VoiceEnrollmentScreen(onComplete = { navController.popBackStack() })
        }
        composable(Routes.MEMORIES) {
            MemoryBrowserScreen(viewModel = hiltViewModel())
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToPersonality = { navController.navigate(Routes.PERSONALITY_SETUP) },
                onNavigateToVoiceEnrollment = { navController.navigate(Routes.VOICE_ENROLLMENT) },
                onNavigateToMemories = { navController.navigate(Routes.MEMORIES) }
            )
        }
    }
}
