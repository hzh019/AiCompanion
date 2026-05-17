package com.aicompanion.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicompanion.conversation.ConversationState
import com.aicompanion.data.local.entity.MessageEntity
import com.aicompanion.ui.viewmodel.ConversationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel = hiltViewModel(),
    onNavigateToPersonality: () -> Unit = {},
    onNavigateToMemories: () -> Unit = {},
    onNavigateToVoiceEnrollment: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val textInput by viewModel.textInput.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-scroll to latest message
    LaunchedEffect(uiState.messages.size, uiState.responseText) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Show errors
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("AI 伴侣")
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusChip(uiState.conversationState)
                    }
                },
                actions = {
                    // Voice enrollment
                    IconButton(onClick = onNavigateToVoiceEnrollment) {
                        Icon(Icons.Default.VoiceOverOff, "声音注册")
                    }
                    // Memories
                    IconButton(onClick = onNavigateToMemories) {
                        Icon(Icons.Default.Psychology, "记忆")
                    }
                    // Personality
                    IconButton(onClick = onNavigateToPersonality) {
                        Icon(Icons.Default.Settings, "设置")
                    }
                }
            )
        },
        bottomBar = {
            BottomInputBar(
                textInput = textInput,
                onTextChange = { viewModel.updateTextInput(it) },
                onSend = { viewModel.sendTextMessage() },
                onMicClick = { viewModel.startVoiceMode() },
                isListening = uiState.conversationState is ConversationState.Listening,
                isProcessing = uiState.conversationState is ConversationState.Thinking
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Always-listening toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "始终聆听",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = uiState.isAlwaysListening,
                    onCheckedChange = { viewModel.toggleAlwaysListening() }
                )
            }

            // Message list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Welcome message if empty
                if (uiState.messages.isEmpty() && uiState.partialText.isBlank()) {
                    item {
                        WelcomeMessage()
                    }
                }

                // Messages
                items(uiState.messages, key = { it.id }) { message ->
                    ChatBubble(message = message)
                }

                // Streaming response (not yet saved as message)
                if (uiState.responseText.isNotBlank() &&
                    uiState.conversationState is ConversationState.Thinking) {
                    item {
                        ChatBubble(
                            message = MessageEntity(
                                conversationId = "",
                                role = "assistant",
                                content = uiState.responseText
                            ),
                            isStreaming = true
                        )
                    }
                }

                // Speaking indicator
                if (uiState.conversationState is ConversationState.Speaking &&
                    uiState.responseText.isNotBlank()) {
                    item {
                        ChatBubble(
                            message = MessageEntity(
                                conversationId = "",
                                role = "assistant",
                                content = uiState.responseText
                            ),
                            isSpeaking = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(state: ConversationState) {
    val (text, color) = when (state) {
        is ConversationState.Idle -> "待机中" to MaterialTheme.colorScheme.outline
        is ConversationState.Listening -> "聆听中..." to Color(0xFF4CAF50)
        is ConversationState.Processing -> "处理中..." to Color(0xFFFF9800)
        is ConversationState.Thinking -> "思考中..." to Color(0xFF2196F3)
        is ConversationState.Speaking -> "说话中..." to Color(0xFF9C27B0)
        is ConversationState.Error -> "错误" to MaterialTheme.colorScheme.error
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ChatBubble(
    message: MessageEntity,
    isStreaming: Boolean = false,
    isSpeaking: Boolean = false
) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = shape,
            color = bubbleColor
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    message.content + if (isStreaming) "▊" else "",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isSpeaking) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "🔊 播放中...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BottomInputBar(
    textInput: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    isListening: Boolean,
    isProcessing: Boolean
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mic button
            FilledIconButton(
                onClick = onMicClick,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isListening)
                        Color(0xFFE53935) else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    Icons.Default.Mic,
                    "语音",
                    tint = if (isListening) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Text input
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入消息...") },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp)
            )

            // Send button
            FilledIconButton(
                onClick = onSend,
                modifier = Modifier.size(48.dp),
                enabled = textInput.isNotBlank() && !isProcessing
            ) {
                Icon(Icons.Default.Send, "发送")
            }
        }
    }
}

@Composable
fun WelcomeMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "💕",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "你的AI伴侣已就绪",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "输入文字或点击麦克风开始对话",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
