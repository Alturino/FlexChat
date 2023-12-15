package com.onirutla.flexchat.core.data.repository

import com.onirutla.flexchat.core.data.models.Message

class MessageRepository {
    fun getMessageByUserId(userId: String): List<Message> = listOf()
    fun getMessageByConversationId(conversationId: String): List<Message> = listOf()
    fun getMessageByConversationMemberId(conversationMemberId: String): List<Message> = listOf()
}
