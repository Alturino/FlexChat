/*
 * MIT License
 *
 * Copyright (c) 2023 - 2023 Ricky Alturino
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.onirutla.flexchat.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.onirutla.flexchat.core.data.FirebaseCollections
import com.onirutla.flexchat.core.data.models.ConversationResponse
import com.onirutla.flexchat.domain.models.Conversation
import com.onirutla.flexchat.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConversationRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
) : ConversationRepository {
    override suspend fun getConversationByUserId(userId: String): List<Conversation> {
        TODO("Not yet implemented")
    }

    override suspend fun getConversationById(conversationId: String): Conversation {
        val conversationResponse = firebaseFirestore.collection(FirebaseCollections.CONVERSATIONS)
            .document(conversationId)
            .get()
            .await()
            .toObject<ConversationResponse>()

    }

    override suspend fun observeConversationByUserId(userId: String): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun createConversation(conversation: Conversation) {
        TODO("Not yet implemented")
    }

}
