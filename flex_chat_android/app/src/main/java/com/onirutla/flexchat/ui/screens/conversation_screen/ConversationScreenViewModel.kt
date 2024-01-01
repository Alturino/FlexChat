package com.onirutla.flexchat.ui.screens.conversation_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onirutla.flexchat.core.data.models.MessageResponse
import com.onirutla.flexchat.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.domain.repository.ConversationRepository
import com.onirutla.flexchat.domain.repository.MessageRepository
import com.onirutla.flexchat.domain.repository.UserRepository
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
        viewModelScope.launch {
            conversationRepository.getConversationById(conversationId)
                .onRight { conversation ->
                    _state.update { it.copy(conversation = conversation) }
                }
        }
    }

    init {
        viewModelScope.launch {
            launch {
                userRepository.currentUser
                    .collect { currentUser ->
                        _state.update { it.copy(currentUser = currentUser) }
                    }
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
                    .distinctUntilChanged { old, new -> old == new }
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
        when (event) {
            is ConversationScreenEvent.OnDraftMessageChanged -> {
                _state.update { it.copy(draftMessage = event.draftMessage) }
            }

            ConversationScreenEvent.OnDraftMessageSend -> {
                viewModelScope.launch {
                    messageRepository.createMessage(
                        MessageResponse(
                            userId = _state.value.currentUser.id,
                            conversationId = _state.value.conversation.id,
                            conversationMemberId = _state.value.currentConversationMember.id,
                            senderName = _state.value.currentUser.username,
                            senderPhotoUrl = _state.value.currentUser.photoProfileUrl,
                            messageBody = _state.value.draftMessage,
                        )
                    ).onLeft { exception ->
                        Timber.e(exception)
                        _state.update {
                            it.copy(
                                isSendMessageError = true,
                                sendErrorMessage = exception.localizedMessage.orEmpty()
                            )
                        }
                    }.onRight { result ->
                        Timber.d("message created with message id:$result")
                        _state.update { it.copy(isSendMessageError = false, sendErrorMessage = "") }
                    }
                    _state.update { it.copy(draftMessage = "") }
                }
            }

            ConversationScreenEvent.OnHandleErrorMessage -> {
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
