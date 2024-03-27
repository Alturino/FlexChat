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

package com.onirutla.flexchat.conversation.ui.add_new_conversation_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import arrow.fx.coroutines.parMap
import com.onirutla.flexchat.auth.domain.repository.AuthRepository
import com.onirutla.flexchat.conversation.domain.model.Conversation
import com.onirutla.flexchat.conversation.domain.model.ConversationMember
import com.onirutla.flexchat.conversation.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.ui.screens.add_new_conversation_screen.AddNewConversationScreenEvent
import com.onirutla.flexchat.user.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddNewConversationScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository,
    private val conversationMemberRepository: ConversationMemberRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddNewConversationScreenState())
    val state = _state.asStateFlow()

    init {
        authRepository.currentUser
            .onEach { user -> _state.update { it.copy(currentUser = user) } }
            .launchIn(viewModelScope)

        _state.map { it.query }
            .filterNot { it.isEmpty() or it.isBlank() }
            .debounce(500)
            .onEach { Timber.d("query: $it") }
            .flatMapLatest { userRepository.getUserByUsername(it) }
            .map { users -> users.filterNot { it.id == _state.value.currentUser?.id } }
            .onEach { users -> _state.update { it.copy(users = users) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: AddNewConversationScreenEvent) {
        when (event) {
            is AddNewConversationScreenEvent.OnQueryChange -> {
                _state.update { it.copy(query = event.query) }
            }

            AddNewConversationScreenEvent.OnQueryClear -> {
                _state.update { it.copy(query = "") }
            }

            AddNewConversationScreenEvent.OnSearchClick -> {

            }

            is AddNewConversationScreenEvent.OnUserItemClick -> {
                viewModelScope.launch {
                    val conversationMemberNames = listOf(
                        event.user.username,
                        _state.value.currentUser?.username.orEmpty(),
                    )
                    val conversationId = conversationRepository.createConversation(
                        Conversation(
                            id = "",
                            conversationName = conversationMemberNames.joinToString(separator = " "),
                            slug = conversationMemberNames.sorted()
                                .joinToString(separator = ",") { it.lowercase() },
                        )
                    ).onRight { Timber.d("conversationId: $it") }
                        .onLeft { Timber.e(it) }
                        .getOrElse { "" }

                    if (conversationId.isNotBlank() or conversationId.isNotEmpty()) {
                        listOf(event.user, _state.value.currentUser).parMap {
                            ConversationMember(
                                id = "",
                                userId = it?.id.orEmpty(),
                                conversationId = conversationId,
                                username = it?.username.orEmpty(),
                                photoProfileUrl = it?.photoProfileUrl.orEmpty(),
                                email = it?.email.orEmpty(),
                            )
                        }.parMap { conversationMemberResponse ->
                            conversationMemberRepository.createConversationMember(
                                conversationMemberResponse
                            )
                                .onLeft { Timber.e(it) }
                                .onRight { Timber.d("conversationMemberId $it") }
                        }
                        _state.update { it.copy(conversationId = conversationId) }
                    }
                }
            }
        }
    }
}
