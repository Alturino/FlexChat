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

package com.onirutla.flexchat.ui.screens.conversation_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onirutla.flexchat.conversation.data.model.MessageResponse
import com.onirutla.flexchat.conversation.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.conversation.domain.repository.MessageRepository
import com.onirutla.flexchat.user.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationScreenViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val conversationMemberRepository: ConversationMemberRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationScreenState())
    val state = _state.asStateFlow()

    operator fun invoke(conversationId: String) {
        Timber.d("conversationId: $conversationId")
        viewModelScope.launch {
            conversationRepository.getConversationById(conversationId)
                .onLeft { Timber.e(it) }
                .onRight { conversation ->
                    _state.update { state ->
                        state.copy(
                            conversation = conversation.copy(
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
        viewModelScope.launch {
            launch {
//                userRepository.currentUser
//                    .collect { currentUser ->
//                        _state.update { it.copy(currentUser = currentUser) }
//                    }
            }
            launch {
                _state.mapLatest { it.conversation.id }
                    .filterNot { it.isEmpty() or it.isBlank() }
                    .flatMapLatest { messageRepository.observeMessageByConversationId(it) }
                    .collect { messages ->
                        _state.update { it.copy(messages = messages) }
                    }
            }
            launch {
                _state.mapLatest { it.currentUser.id }
                    .filterNot { it.isEmpty() or it.isBlank() }
                    .distinctUntilChanged()
                    .flatMapLatest { userId ->
                        conversationMemberRepository.observeConversationMemberByUserId(userId)
                            .mapNotNull { conversationMembers ->
                                conversationMembers.firstOrNull { it.userId == userId }
                            }
                    }
                    .collect { conversationMember ->
                        _state.update { it.copy(currentConversationMember = conversationMember) }
                    }
            }
        }
    }

    fun onEvent(event: ConversationScreenEvent) {
//        when (event) {
//            is ConversationScreenEvent.OnDraftMessageChanged -> {
//                _state.update { it.copy(draftMessage = event.draftMessage) }
//            }
//
//            ConversationScreenEvent.OnDraftMessageSend -> {
//                viewModelScope.launch {
//                    messageRepository.createMessage(
//                        MessageResponse(
//                            userId = _state.value.currentUser.id,
//                            conversationId = _state.value.conversation.id,
//                            conversationMemberId = _state.value.currentConversationMember.id,
//                            senderName = _state.value.currentUser.username,
//                            senderPhotoUrl = _state.value.currentUser.photoProfileUrl,
//                            messageBody = _state.value.draftMessage,
//                        )
//                    ).onLeft { exception ->
//                        Timber.e(exception)
//                        _state.update {
//                            it.copy(
//                                isSendMessageError = true,
//                                sendErrorMessage = exception.localizedMessage.orEmpty()
//                            )
//                        }
//                    }.onRight { result ->
//                        Timber.d("message created with message id:$result")
//                        _state.update { it.copy(isSendMessageError = false, sendErrorMessage = "") }
//                    }
//                    _state.update { it.copy(draftMessage = "") }
//                }
//            }
//
//            ConversationScreenEvent.OnHandleErrorMessage -> {
//                _state.update {
//                    it.copy(
//                        isSendMessageError = !it.isSendMessageError,
//                        sendErrorMessage = ""
//                    )
//                }
//            }
//        }
    }

}
