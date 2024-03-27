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

package com.onirutla.flexchat.ui.screens.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.coroutines.parMap
import com.google.firebase.messaging.FirebaseMessaging
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.user.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseMessaging: FirebaseMessaging,
    private val conversationRepository: ConversationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState())
    val state = _state.asStateFlow()

    private val userId = _state.mapLatest { it.userId }
        .distinctUntilChanged { old, new -> old == new }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    init {
        viewModelScope.launch {
//            userRepository.currentUser
//                .map { it.id }
//                .onEach { userId -> _state.update { it.copy(userId = userId) } }
//                .launchIn(viewModelScope)
//            userId.flatMapLatest { conversationRepository.observeConversationByUserId(it) }
//                .map { conversations -> conversations.filterNot { it.conversationMembers.isEmpty() and it.messages.isEmpty() } }
//                .onEach { conversations ->
//                    _state.update { it.copy(conversations = conversations) }
//                }
//                .launchIn(viewModelScope)
//            _state.mapLatest { it.conversations }
//                .filterNot { it.isEmpty() }
//                .distinctUntilChanged()
//                .onEach { conversations ->
//                    conversations.parMap { conversation ->
//                        firebaseMessaging.subscribeToTopic(conversation.id)
//                            .addOnFailureListener { Timber.e(it) }
//                            .addOnSuccessListener { Timber.d("Subscribed to topic: ${conversation.id}") }
//                            .addOnCompleteListener { Timber.d("Complete subscribing to topic: ${conversation.id}") }
//                    }
//                }.launchIn(viewModelScope)
        }
    }

    operator fun invoke() {
        viewModelScope.launch {
//            val conversations = conversationRepository.getConversationByUserId(_state.value.userId)
//                .onLeft { Timber.e(it) }
//                .onRight { Timber.d("$it") }
//                .fold(ifLeft = { listOf() }, ifRight = { it })
//            _state.update { it.copy(conversations = conversations) }
        }
    }

}
