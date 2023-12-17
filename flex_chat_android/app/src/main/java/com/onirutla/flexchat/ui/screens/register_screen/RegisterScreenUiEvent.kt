package com.onirutla.flexchat.ui.screens.register_screen

sealed interface RegisterScreenUiEvent {
    data object OnRegisterWithGoogleClick : RegisterScreenUiEvent
    data object OnHaveAnAccountClick : RegisterScreenUiEvent
}
