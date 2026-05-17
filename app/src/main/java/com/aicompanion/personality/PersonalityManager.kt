package com.aicompanion.personality

import com.aicompanion.data.local.entity.PersonalityEntity
import com.aicompanion.data.repository.PersonalityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalityManager @Inject constructor(
    private val personalityRepository: PersonalityRepository
) {
    fun getAllPersonalities(): Flow<List<PersonalityEntity>> {
        return personalityRepository.getAll()
    }

    suspend fun getActivePersonality(): PersonalityEntity? {
        return personalityRepository.getActive()
    }

    suspend fun setActivePersonality(id: String) {
        personalityRepository.setActive(id)
    }

    suspend fun createPersonality(config: PersonalityConfig): PersonalityEntity {
        val entity = PersonalityEntity(
            name = config.name,
            characterName = config.characterName,
            relationship = config.relationship,
            personalityTraits = config.personalityTraits,
            speakingStyle = config.speakingStyle,
            backstory = config.backstory,
            userNickname = config.userNickname,
            language = config.language,
            voiceId = config.voiceId,
            systemPromptOverride = config.systemPromptOverride
        )
        personalityRepository.insert(entity)
        return entity
    }

    suspend fun updatePersonality(entity: PersonalityEntity) {
        personalityRepository.update(entity)
    }

    suspend fun deletePersonality(entity: PersonalityEntity) {
        personalityRepository.delete(entity)
    }

    suspend fun ensureDefaultPersonality() {
        val existing = personalityRepository.getAll().firstOrNull()
        if (existing.isNullOrEmpty()) {
            // Create default personality from template
            val default = PersonalityTemplates.templates.first()
            val entity = createPersonality(default)
            setActivePersonality(entity.id)
        } else {
            // Ensure there's an active personality
            val active = getActivePersonality()
            if (active == null && existing.isNotEmpty()) {
                setActivePersonality(existing.first().id)
            }
        }
    }
}
