package com.onirutla.flexchat.core.data.models

data class User(
    val id: String = "",
    val username: String = "",
    val photoProfileUrl: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val isOnline: Boolean = false,
    val conversationMembers: List<ConversationMember> = listOf(),
    val conversation: List<Conversation> = listOf(),
    val createdAt: String = "",
    val deletedAt: String = "",
)
