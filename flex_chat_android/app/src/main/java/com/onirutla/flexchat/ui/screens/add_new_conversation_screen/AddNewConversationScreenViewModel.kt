/*
 * MIT License
 *
 * Copyright (c) 2023 Ricky Alturino
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

package com.onirutla.flexchat.ui.screens.add_new_conversation_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.coroutines.parMap
import com.onirutla.flexchat.core.data.models.ConversationMemberResponse
import com.onirutla.flexchat.core.data.models.ConversationResponse
import com.onirutla.flexchat.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.domain.repository.ConversationRepository
import com.onirutla.flexchat.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class AddNewConversationScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository,
    private val conversationMemberRepository: ConversationMemberRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddNewConversationScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                userRepository.currentUser.collectLatest { user ->
                    _state.update { it.copy(currentUser = user) }
                }
            }
            launch {
                _state.mapLatest { it.query }
                    .filterNot { it.isEmpty() or it.isBlank() }
                    .debounce(500)
                    .onEach { Timber.d("query afterDebounce: $it") }
                    .flatMapLatest { userRepository.getUserByUsername(it) }
                    .mapLatest { users ->
                        users.onEach { Timber.d("before filtered: $it") }
                            .filter { user -> user.id != _state.value.currentUser.id }
                            .onEach { Timber.d("after filtered: $it") }
                    }
                    .collect { users ->
                        _state.update { it.copy(users = users) }
                    }
            }
        }
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
                        _state.value.currentUser.username,
                    )
                    val conversationId = conversationRepository.createConversation(
                        ConversationResponse(
                            conversationName = conversationMemberNames.joinToString(separator = " "),
                            slug = conversationMemberNames.sorted()
                                .joinToString(separator = ",") { it.lowercase() },
                        )
                    ).onRight { Timber.d("conversationId: $it") }
                        .fold(ifLeft = { "" }, ifRight = { it })

                    if (conversationId.isNotBlank() or conversationId.isNotEmpty()) {
                        listOf(event.user, _state.value.currentUser).parMap {
                            ConversationMemberResponse(
                                userId = it.id,
                                conversationId = conversationId,
                                username = it.username,
                                photoProfileUrl = it.photoProfileUrl,
                            )
                        }.parMap { conversationMemberResponse ->
                            conversationMemberRepository.createConversationMember(
                                conversationMemberResponse = conversationMemberResponse
                            ).onRight { Timber.d("conversationMemberId $it") }
                        }
                        _state.update { it.copy(conversationId = conversationId) }
                    }
                }
            }
        }
    }
}
