package com.onirutla.flexchat.ui.screens.login_screen

sealed interface LoginScreenEvent {
    data class OnEmailChange(val email: String) : LoginScreenEvent
    data class OnPasswordChange(val password: String) : LoginScreenEvent
    data class OnIsPasswordVisibleChange(val isPasswordVisible: Boolean) : LoginScreenEvent
    data object OnLoginClicked : LoginScreenEvent
}
