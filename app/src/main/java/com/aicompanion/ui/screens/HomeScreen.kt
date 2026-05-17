package com.aicompanion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToConversation: () -> Unit = {},
    onNavigateToPersonality: () -> Unit = {},
    onNavigateToMemories: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AI Companion", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onNavigateToConversation, modifier = Modifier.fillMaxWidth()) {
            Text("开始对话")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onNavigateToPersonality, modifier = Modifier.fillMaxWidth()) {
            Text("AI 人格设置")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onNavigateToMemories, modifier = Modifier.fillMaxWidth()) {
            Text("查看记忆")
        }
    }
}
