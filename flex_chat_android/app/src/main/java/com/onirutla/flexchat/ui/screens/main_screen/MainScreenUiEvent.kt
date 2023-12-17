package com.onirutla.flexchat.ui.screens.main_screen

import com.onirutla.flexchat.core.data.models.Conversation

sealed interface MainScreenUiEvent {
    data class OnConversationClick(val conversation: Conversation) : MainScreenUiEvent
    data object OnProfileIconClick : MainScreenUiEvent
    data object OnFloatingActionButtonClick : MainScreenUiEvent
}
