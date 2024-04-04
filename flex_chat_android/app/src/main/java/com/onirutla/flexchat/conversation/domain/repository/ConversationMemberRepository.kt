/*
 * Copyright 2024 Ricky Alturino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onirutla.flexchat.conversation.domain.repository

import arrow.core.Either
import com.onirutla.flexchat.conversation.domain.model.ConversationMember
import kotlinx.coroutines.flow.Flow

interface ConversationMemberRepository {
    fun conversationMemberByUserIdFlow(userId: String): Flow<List<ConversationMember>>
    suspend fun conversationMemberByConversationId(conversationId: String): Either<Throwable, List<ConversationMember>>
    suspend fun conversationMemberByUserId(userId: String): Either<Throwable, List<ConversationMember>>
    suspend fun createConversationMember(conversationId: String, userId: String): Either<Throwable, ConversationMember>
    suspend fun conversationMemberById(id: String): Either<Throwable, ConversationMember>
}
