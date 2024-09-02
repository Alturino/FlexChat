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

package com.onirutla.flexchat.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.onirutla.flexchat.auth.domain.repository.AuthRepository
import com.onirutla.flexchat.conversation.data.model.OnGoingCall
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.core.util.FirebaseCollections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class MainActivityViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val conversationRepository: ConversationRepository,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean?> = authRepository.isLoggedIn.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val conversations = authRepository.currentUserFlow
        .map { it.id }
        .filterNot { it.isBlank() or it.isEmpty() }
        .flatMapLatest { conversationRepository.conversationsByUserIdFlow(it) }


    fun test(onGoingCallId: String): Flow<OnGoingCall> {
        return firestore.collection(FirebaseCollections.ONGOING_CALLS)
            .document(onGoingCallId)
            .snapshots()
            .mapNotNull { it.toObject<OnGoingCall>() }
    }
//    val incomingCall = combine(conversations, ongoingCalls) { conversations, onGoingCalls ->
//        onGoingCalls.firstNotNullOfOrNull { conve }
//    }

//    val userId = authRepository.currentUserFlow.map { it.id }
//    val conversations = userId.flatMapLatest { conversationRepository.conversationByUserIdFlow(it) }
//    private val ongoingCall = firestore.collection(FirebaseCollections.ONGOING_CALL)
//        .snapshots()
//        .map { it.toObjects<OngoingCallResponse>() }
//
//    val incomingCall = combine(
//        flow = userId,
//        flow2 = conversations,
//        flow3 = ongoingCall
//    ) { userId, conversations, ongoingCall ->
//
//    }

//    val incomingCall: StateFlow<OngoingCallResponse?> = authRepository.currentUserFlow
//        .onEach { Timber.d("$it") }
//        .flatMapLatest { conversationRepository.conversationByUserIdFlow(it.id) }
//        .onEach { Timber.d("$it") }
//        .map { conversations ->
//            conversations.firstNotNullOfOrNull {
//                firestore.collection(FirebaseCollections.ONGOING_CALL)
//                    .document(it.id)
//                    .get()
//                    .await()
//                    .toObject<OngoingCallResponse>()
//            }
//        }
//        .onEach { Timber.d("$it") }
//        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
}
