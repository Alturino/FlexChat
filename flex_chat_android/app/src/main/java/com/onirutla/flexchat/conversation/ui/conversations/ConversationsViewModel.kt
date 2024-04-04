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

package com.onirutla.flexchat.conversation.ui.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import arrow.fx.coroutines.parMap
import com.google.firebase.messaging.FirebaseMessaging
import com.onirutla.flexchat.auth.domain.repository.AuthRepository
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseMessaging: FirebaseMessaging,
    private val conversationRepository: ConversationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationsState())
    val state = _state.asStateFlow()

    init {
        authRepository.currentUserFlow
            .map { it.id }
            .onEach { userId -> _state.update { it.copy(userId = userId) } }
            .launchIn(viewModelScope)

        _state.map { it.userId }
            .flatMapLatest { conversationRepository.conversationsByUserIdFlow(it) }
            .distinctUntilChanged()
            .onEach { conversations ->
                _state.update { state ->
                    state.copy(conversations = conversations.sortedByDescending { conversation ->
                        conversation.messages.maxOf { it.createdAt }
                    })
                }
            }
            .distinctUntilChanged()
            .launchIn(viewModelScope)

        _state.map { it.conversations }
            .filterNot { it.isEmpty() }
            .distinctUntilChanged { old, new -> old.containsAll(new) }
            .onEach { conversations ->
                conversations.parMap { conversation ->
                    firebaseMessaging.subscribeToTopic(conversation.id)
                        .addOnFailureListener { Timber.e(it) }
                        .addOnSuccessListener { Timber.d("Subscribed to topic: ${conversation.id}") }
                        .addOnCompleteListener { Timber.d("Complete subscribing to topic: ${conversation.id}") }
                }
            }
            .launchIn(viewModelScope)
    }

    operator fun invoke() {
        viewModelScope.launch {
            val conversations = conversationRepository.conversationByUserId(_state.value.userId)
                .onLeft { Timber.e(it) }
                .onRight { Timber.d("$it") }
                .getOrElse { listOf() }
            _state.update { it.copy(conversations = conversations) }
        }
    }

}
