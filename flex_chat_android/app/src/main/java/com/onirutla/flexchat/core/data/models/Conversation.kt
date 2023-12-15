package com.onirutla.flexchat.core.data.models

data class Conversation(
    val id: String = "",
    val name: String = "",
    val isGroup: Boolean = false,
    val conversationMembers: List<ConversationMember> = listOf(),
    val users: List<User> = listOf(),
    val createdAt: String = "",
    val deletedAt: String = "",
)

