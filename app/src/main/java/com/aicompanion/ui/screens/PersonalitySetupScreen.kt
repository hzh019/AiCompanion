package com.aicompanion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aicompanion.data.local.entity.PersonalityEntity
import com.aicompanion.personality.PersonalityConfig
import com.aicompanion.personality.PersonalityTemplates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalitySetupScreen(
    personalities: List<PersonalityEntity> = emptyList(),
    activePersonalityId: String? = null,
    onSelectPersonality: (String) -> Unit = {},
    onCreateFromTemplate: (PersonalityConfig) -> Unit = {},
    onDeletePersonality: (PersonalityEntity) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 人格") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active personalities
            if (personalities.isNotEmpty()) {
                item {
                    Text(
                        "已创建的人格",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(personalities, key = { it.id }) { personality ->
                    PersonalityCard(
                        personality = personality,
                        isActive = personality.id == activePersonalityId,
                        onSelect = { onSelectPersonality(personality.id) },
                        onDelete = { onDeletePersonality(personality) }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Templates
            item {
                Text(
                    "从模板创建",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(PersonalityTemplates.templates) { template ->
                TemplateCard(
                    config = template,
                    onClick = { onCreateFromTemplate(template) }
                )
            }
        }
    }
}

@Composable
fun PersonalityCard(
    personality: PersonalityEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        personality.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("使用中", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${personality.relationship} · ${personality.characterName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    personality.personalityTraits,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isActive) {
                IconButton(onClick = onSelect) {
                    Icon(Icons.Default.Check, "选择")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun TemplateCard(
    config: PersonalityConfig,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    config.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "${config.characterName} · ${config.relationship}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                config.personalityTraits,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                config.speakingStyle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
