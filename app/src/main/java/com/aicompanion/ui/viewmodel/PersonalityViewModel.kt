package com.aicompanion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicompanion.data.local.entity.PersonalityEntity
import com.aicompanion.personality.PersonalityConfig
import com.aicompanion.personality.PersonalityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalityViewModel @Inject constructor(
    private val personalityManager: PersonalityManager
) : ViewModel() {

    private val _personalities = MutableStateFlow<List<PersonalityEntity>>(emptyList())
    val personalities: StateFlow<List<PersonalityEntity>> = _personalities.asStateFlow()

    private val _activeId = MutableStateFlow<String?>(null)
    val activeId: StateFlow<String?> = _activeId.asStateFlow()

    init {
        viewModelScope.launch {
            personalityManager.ensureDefaultPersonality()
            personalityManager.getAllPersonalities().collect { list ->
                _personalities.value = list
                _activeId.value = list.firstOrNull { it.isActive }?.id
            }
        }
    }

    fun setActive(id: String) {
        viewModelScope.launch {
            personalityManager.setActivePersonality(id)
        }
    }

    fun createFromTemplate(config: PersonalityConfig) {
        viewModelScope.launch {
            personalityManager.createPersonality(config)
        }
    }

    fun delete(entity: PersonalityEntity) {
        viewModelScope.launch {
            personalityManager.deletePersonality(entity)
        }
    }
}
