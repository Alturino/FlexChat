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

package com.onirutla.flexchat.conversation.ui.conversation_room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onirutla.flexchat.auth.domain.repository.AuthRepository
import com.onirutla.flexchat.conversation.domain.model.Message
import com.onirutla.flexchat.conversation.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.conversation.domain.repository.MessageRepository
import com.onirutla.flexchat.user.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationRoomViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val authRepository: AuthRepository,
    private val conversationMemberRepository: ConversationMemberRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationRoomState())
    val state = _state.asStateFlow()

    operator fun invoke(conversationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            conversationRepository.getConversationById(conversationId)
                .onLeft { Timber.e(it) }
                .onRight { conversation ->
                    _state.update { state ->
                        state.copy(
                            conversation = conversation.copy(
                                // TODO: Refactor this
                                // To not include current user, username on one on one chat
                                conversationName = conversation.conversationName
                                    .split(" ")
                                    .filterNot { it == _state.value.currentUser.username }
                                    .joinToString("")
                            )
                        )
                    }
                }
        }
    }

    init {
        authRepository.currentUser
            .filterNot { it == User() }
            .onEach { user -> _state.update { it.copy(currentUser = user) } }
            .launchIn(viewModelScope)

        _state.map { it.conversation.id }
            .filterNot { it.isBlank() or it.isEmpty() }
            .distinctUntilChanged { old, new -> old == new }
            .flatMapLatest { messageRepository.messageByConversationIdFlow(it) }
            .distinctUntilChanged()
            .onEach { messages -> _state.update { state -> state.copy(messages = messages.sortedByDescending { it.createdAt }) } }
            .launchIn(viewModelScope)

        _state.map { it.currentUser.id }
            .filterNot { it.isEmpty() or it.isBlank() }
            .distinctUntilChanged { old, new -> old == new }
            .flatMapLatest { userId ->
                conversationMemberRepository.conversationMemberByUserIdFlow(userId)
                    .mapNotNull { conversationMembers ->
                        conversationMembers.firstOrNull { it.userId == userId }
                    }
            }
            .onEach { conversationMember -> _state.update { it.copy(currentConversationMember = conversationMember) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ConversationRoomEvent) {
        when (event) {
            is ConversationRoomEvent.OnDraftMessageChanged -> {
                _state.update { it.copy(draftMessage = event.draftMessage) }
            }

            ConversationRoomEvent.OnDraftMessageSend -> {
                viewModelScope.launch {
                    val message = Message(
                        userId = _state.value.currentUser.id,
                        conversationId = _state.value.conversation.id,
                        conversationMemberId = _state.value.currentConversationMember.id,
                        senderName = _state.value.currentUser.username,
                        senderPhotoUrl = _state.value.currentUser.photoProfileUrl,
                        messageBody = _state.value.draftMessage,
                    )
                    messageRepository.sendMessage(messageRequest = message)
                        .onLeft { exception ->
                            Timber.e(exception)
                            _state.update {
                                it.copy(
                                    isSendMessageError = true,
                                    sendErrorMessage = exception.localizedMessage.orEmpty()
                                )
                            }
                        }.onRight { result ->
                            Timber.d("message created with message id:$result")
                            _state.update {
                                it.copy(
                                    isSendMessageError = false,
                                    sendErrorMessage = ""
                                )
                            }
                        }
                }
                _state.update { it.copy(draftMessage = "") }
            }

            ConversationRoomEvent.OnHandleErrorMessage -> {
                _state.update {
                    it.copy(
                        isSendMessageError = !it.isSendMessageError,
                        sendErrorMessage = ""
                    )
                }
            }

        }
    }

}