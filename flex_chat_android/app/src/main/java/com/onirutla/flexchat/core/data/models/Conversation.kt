package com.onirutla.flexchat.core.data.models

data class Conversation(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val isGroup: Boolean = false,
    val conversationMembers: List<ConversationMember> = listOf(),
    val latestMessage: String = conversationMembers.flatMap { it.messages }.maxByOrNull { it.createdAt }?.messageBody.orEmpty(),
    val createdAt: String = "",
    val deletedAt: String = "",
)

