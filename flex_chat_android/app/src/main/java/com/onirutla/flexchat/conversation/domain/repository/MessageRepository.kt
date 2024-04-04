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

import android.net.Uri
import arrow.core.Either
import com.onirutla.flexchat.conversation.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun messageByUserId(userId: String): Either<Throwable, List<Message>>
    suspend fun messageById(id: String): Either<Throwable, Message>
    suspend fun messageByConversationId(conversationId: String): Either<Throwable, List<Message>>
    fun messageByConversationIdFlow(conversationId: String): Flow<List<Message>>
    suspend fun messageByConversationMemberId(conversationMemberId: String): Either<Throwable, List<Message>>
    suspend fun sendMessage(messageRequest: Message): Either<Throwable, Message>
    suspend fun sendMessageWithAttachment(message: Message, uri: Uri): Either<Throwable, Unit>
    fun messageByUserIdFlow(userId: String): Flow<List<Message>>
    val observeMessage: Flow<List<Message>>
}
