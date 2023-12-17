package com.onirutla.flexchat.ui.screens.register_screen

sealed interface RegisterScreenEvent {
    data class OnEmailChange(val email: String) : RegisterScreenEvent
    data class OnPasswordChange(val password: String) : RegisterScreenEvent
    data class OnIsPasswordVisibleChange(val isPasswordVisible: Boolean) : RegisterScreenEvent
    data class OnIsConfirmPasswordVisibleChange(val isPasswordVisible: Boolean) :
        RegisterScreenEvent

    data object OnRegisterClick : RegisterScreenEvent
}
