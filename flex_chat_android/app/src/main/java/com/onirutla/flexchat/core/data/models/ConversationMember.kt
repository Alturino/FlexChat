package com.onirutla.flexchat.core.data.models

data class ConversationMember(
    val id: String = "",
    val userId: String = "",
    val conversationId: String = "",
    val username: String = "",
    val photoProfileUrl: String = "",
    val email: String = "",
    val messages: List<Message> = listOf(),
    val joinedAt: String = "",
    val leftAt: String = "",
)
