package com.onirutla.flexchat.ui.screens.login_screen

data class LoginScreenState(
    val email: String = "",
    val isEmailError: Boolean? = null,
    val password: String = "",
    val isPasswordError: Boolean? = null,
    val isPasswordVisible: Boolean = false,
)
