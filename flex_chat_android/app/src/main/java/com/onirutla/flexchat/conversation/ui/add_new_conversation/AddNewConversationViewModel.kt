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

package com.onirutla.flexchat.conversation.ui.add_new_conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.onirutla.flexchat.auth.domain.repository.AuthRepository
import com.onirutla.flexchat.conversation.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.user.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
class AddNewConversationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository,
    private val conversationMemberRepository: ConversationMemberRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddNewConversationState())
    val state = _state.asStateFlow()

    init {
        authRepository.currentUserFlow
            .onEach { user -> _state.update { it.copy(currentUser = user) } }
            .launchIn(viewModelScope)

        _state.map { it.query }
            .filterNot { it.isEmpty() or it.isBlank() }
            .debounce(500)
            .onEach { Timber.d("query: $it") }
            .flatMapLatest { userRepository.userByUsernameFlow(it) }
            .onEach { Timber.d("$it") }
            .map { users -> users.filterNot { it.id == _state.value.currentUser?.id } }
            .onEach { users -> _state.update { it.copy(users = users) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: AddNewConversationEvent) {
        when (event) {
            is AddNewConversationEvent.OnQueryChange -> {
                _state.update { it.copy(query = event.query) }
            }

            AddNewConversationEvent.OnQueryClear -> {
                _state.update { it.copy(query = "") }
            }

            AddNewConversationEvent.OnSearchClick -> {
                viewModelScope.launch(Dispatchers.IO) {
                    userRepository.userByUsername(username = _state.value.query)
                        .onLeft { Timber.e("Failed to fetch user") }
                        .onRight { users -> _state.update { it.copy(users = users) } }
                }
            }

            is AddNewConversationEvent.OnUserItemClick -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val userIds = listOf(event.user.id, _state.value.currentUser?.id.orEmpty())
                        .sorted()
                    conversationRepository.createConversation(userIds)
                        .onLeft { Timber.e(it) }
                        .onRight { conversation ->
                            Timber.d("$conversation")
                            _state.update { it.copy(conversationId = conversation.id) }
                        }
                }
            }

        }
    }
}
