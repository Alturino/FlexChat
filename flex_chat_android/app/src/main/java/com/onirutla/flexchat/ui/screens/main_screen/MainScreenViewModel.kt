package com.onirutla.flexchat.ui.screens.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onirutla.flexchat.domain.repository.ConversationRepository
import com.onirutla.flexchat.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState())
    val state = _state.asStateFlow()

    private val userId = _state.mapLatest { it.userId }
        .distinctUntilChanged { old, new -> old == new }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    init {
        viewModelScope.launch {
            launch {
                userRepository.currentUser.map { it.id }
                    .collect { userId -> _state.update { it.copy(userId = userId) } }
            }
            launch {
                userId.flatMapLatest {
                    conversationRepository.observeConversationByUserId(it)
                }.collect { conversations ->
                    _state.update { it.copy(conversations = conversations) }
                }
            }
        }
    }

    operator fun invoke() {
        viewModelScope.launch {
            val conversations = conversationRepository.getConversationByUserId(_state.value.userId)
                .onRight { Timber.d("$it") }
                .fold(ifLeft = { listOf() }, ifRight = { it })
            _state.update { it.copy(conversations = conversations) }
        }
    }

}
