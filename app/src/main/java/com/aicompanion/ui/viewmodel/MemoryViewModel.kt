package com.aicompanion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicompanion.data.local.entity.MemoryEntity
import com.aicompanion.data.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("全部")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val memories: StateFlow<List<MemoryEntity>> = combine(
        memoryRepository.getAllActive(),
        _selectedCategory
    ) { allMemories, category ->
        if (category == "全部") {
            allMemories
        } else {
            val mappedCategory = when (category) {
                "偏好" -> "preference"
                "个人事实" -> "personal_fact"
                "事件" -> "event"
                "关系" -> "relationship"
                else -> "other"
            }
            allMemories.filter { it.category == mappedCategory }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            memoryRepository.softDeleteMemory(memory.id)
        }
    }
}
