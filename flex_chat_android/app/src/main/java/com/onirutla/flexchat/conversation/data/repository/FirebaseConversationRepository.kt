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

package com.onirutla.flexchat.conversation.data.repository

import arrow.core.Either
import com.google.firebase.firestore.FirebaseFirestore
import com.onirutla.flexchat.conversation.data.model.Conversation
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.conversation.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseConversationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messageRepository: MessageRepository,
) : ConversationRepository {

    override suspend fun conversationByUserId(userId: String): Either<Throwable, List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun getConversationById(conversationId: String): Either<Throwable, Conversation> {
        TODO("Not yet implemented")
    }

    override fun conversationsByUserIdFlow(userId: String): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun createConversation(userIds: List<String>): Either<Throwable, Conversation> {
        TODO("Not yet implemented")
    }

}
