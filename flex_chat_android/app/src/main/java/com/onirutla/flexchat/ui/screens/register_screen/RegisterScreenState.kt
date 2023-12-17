package com.onirutla.flexchat.ui.screens.register_screen

data class RegisterScreenState(
    val email: String = "",
    val isEmailError: Boolean? = null,
    val password: String = "",
    val isPasswordError: Boolean? = null,
    val isPasswordVisible: Boolean = false,
    val confirmPassword: String = "",
    val isConfirmPasswordError: Boolean? = null,
    val isConfirmPasswordVisible: Boolean = false,
    val isRegisterSuccessful: Boolean? = null,
    val registerErrorMessage: String = "",
    val isRegisterWithGoogleSuccessful: Boolean? = null,
    val registerWithGoogleErrorMessage: String = "",
)
