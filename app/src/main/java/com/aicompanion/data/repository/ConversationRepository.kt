package com.aicompanion.data.repository

import com.aicompanion.data.local.dao.ConversationDao
import com.aicompanion.data.local.dao.MessageDao
import com.aicompanion.data.local.entity.ConversationEntity
import com.aicompanion.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class ConversationRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {
    fun getConversationById(id: String): ConversationEntity? {
        return conversationDao.getConversationById(id)
    }

    fun getAllConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    suspend fun insert(conversation: ConversationEntity) {
        conversationDao.insert(conversation)
    }

    suspend fun update(conversation: ConversationEntity) {
        if (conversation != null) {
            conversationDao.update(conversation)
        }
    }

    suspend fun updateConversation(conversation: ConversationEntity?) {
        if (conversation != null) {
            conversationDao.update(conversation)
        }
    }

    suspend fun delete(conversation: ConversationEntity) {
        conversationDao.delete(conversation)
    }

    suspend fun insertMessage(message: MessageEntity) {
        messageDao.insert(message)
    }

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessages(conversationId)
    }
}
