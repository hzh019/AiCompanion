package com.aicompanion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPersonality: () -> Unit = {},
    onNavigateToVoiceEnrollment: () -> Unit = {},
    onNavigateToMemories: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("设置") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // AI Personality
            SettingsSection("AI 人格") {
                SettingsItem(
                    icon = Icons.Default.Face,
                    title = "人格设置",
                    subtitle = "自定义AI的性格和说话方式",
                    onClick = onNavigateToPersonality
                )
            }

            // Voice
            SettingsSection("声音") {
                SettingsItem(
                    icon = Icons.Default.VoiceOverOff,
                    title = "声音注册",
                    subtitle = "录制你的声音，让AI只回应你",
                    onClick = onNavigateToVoiceEnrollment
                )
            }

            // Memory
            SettingsSection("记忆") {
                SettingsItem(
                    icon = Icons.Default.Psychology,
                    title = "记忆管理",
                    subtitle = "查看和管理AI记住的关于你的事情",
                    onClick = onNavigateToMemories
                )
            }

            // Proactive settings placeholder
            SettingsSection("主动消息") {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "早安问候",
                    subtitle = "每天早上AI会主动跟你打招呼",
                    checked = true,
                    onCheckedChange = {}
                )
                SettingsToggleItem(
                    icon = Icons.Default.Nightlight,
                    title = "晚安消息",
                    subtitle = "晚上AI会关心你是否休息好",
                    checked = true,
                    onCheckedChange = {}
                )
                SettingsToggleItem(
                    icon = Icons.Default.Favorite,
                    title = "日常关心",
                    subtitle = "AI会不定期发消息关心你",
                    checked = true,
                    onCheckedChange = {}
                )
            }

            // About
            SettingsSection("关于") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本",
                    subtitle = "1.0.0 (MVP)",
                    onClick = {}
                )
                SettingsItem(
                    icon = Icons.Default.Build,
                    title = "技术栈",
                    subtitle = "Kotlin + Jetpack Compose + DeepSeek",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
