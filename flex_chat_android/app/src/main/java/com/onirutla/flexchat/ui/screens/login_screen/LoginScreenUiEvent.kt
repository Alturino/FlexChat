package com.onirutla.flexchat.ui.screens.login_screen

sealed interface LoginScreenUiEvent {
    data object OnForgotPasswordClick : LoginScreenUiEvent
}
