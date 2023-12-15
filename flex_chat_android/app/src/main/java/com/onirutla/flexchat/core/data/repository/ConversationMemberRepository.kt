package com.onirutla.flexchat.core.data.repository

import com.onirutla.flexchat.core.data.models.ConversationMember

class ConversationMemberRepository {
    fun getConversationMemberByUserId(userId: String): List<ConversationMember> = listOf()
    fun getConversationMemberByConversationId(conversationId: String): List<ConversationMember> =
        listOf()
}
