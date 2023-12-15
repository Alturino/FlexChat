package com.onirutla.flexchat.core.data.models

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val conversationMemberId: String = "",
    val userId: String = "",
    val senderName: String = "",
    val messageBody: String = "",
    val createdAt: String = "",
    val deletedAt: String = "",
)
