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

package com.onirutla.flexchat.ui.screens.register_screen

import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.onirutla.flexchat.core.util.isValidEmail
import com.onirutla.flexchat.domain.models.RegisterWithUsernameEmailAndPassword
import com.onirutla.flexchat.domain.repository.UserRepository
import com.onirutla.flexchat.domain.util.isValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RegisterScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterScreenState())
    val state = _state.asStateFlow()

    private val isEmailError = _state
        .mapLatest { it.email }
        .filterNot { it.isEmpty() || it.isBlank() }
        .mapLatest { !it.isValidEmail() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val password = _state
        .mapLatest { it.password }
        .filterNot { it.isEmpty() || it.isBlank() }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
        )

    private val isPasswordError = password
        .mapLatest { !it.isValidPassword() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    private val confirmPassword = _state
        .mapLatest { it.confirmPassword }
        .filterNot { it.isEmpty() || it.isBlank() }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
        )

    private val isConfirmPasswordError = confirmPassword
        .mapLatest { !it.isValidPassword() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val isPasswordSameAsConfirmPassword = merge(
        password.mapLatest { it == _state.value.confirmPassword },
        confirmPassword.mapLatest { it == _state.value.password }
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    private val isRegisterButtonEnabled = combine(
        isEmailError,
        isPasswordError,
        isConfirmPasswordError,
        isPasswordSameAsConfirmPassword
    ) { isEmailError, isPasswordError, isConfirmPasswordError, isPasswordSameAsConfirmPassword ->
        (isEmailError == false) && (isPasswordError == false) && (isConfirmPasswordError == false) && isPasswordSameAsConfirmPassword
    }.distinctUntilChanged { old, new -> old == new }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    init {
        viewModelScope.launch {
            launch {
                isEmailError.collect { isEmailError ->
                    _state.update { it.copy(isEmailError = isEmailError) }
                }
            }
            launch {
                isPasswordError.collect { isPasswordError ->
                    _state.update { it.copy(isPasswordError = isPasswordError) }
                }
            }
            launch {
                isConfirmPasswordError.collect { isConfirmPasswordError ->
                    _state.update { it.copy(isConfirmPasswordError = isConfirmPasswordError) }
                }
            }
            launch {
                isRegisterButtonEnabled.collect { isRegisterButtonEnabled ->
                    _state.update { it.copy(isRegisterButtonEnabled = isRegisterButtonEnabled) }
                }
            }
        }
    }


    fun registerWithGoogle(intent: Intent) {
        viewModelScope.launch {
            userRepository.loginWithGoogle(intent)
                .onLeft { exception ->
                    Timber.e("registerFailed with exception: $exception")
                    _state.update {
                        it.copy(
                            isRegisterWithGoogleSuccessful = false,
                            registerWithGoogleErrorMessage = exception.localizedMessage.orEmpty()
                        )
                    }
                }
                .onRight { user ->
                    Timber.d("loginSuccessful: $user")
                    _state.update {
                        it.copy(
                            isRegisterWithGoogleSuccessful = true,
                            registerWithGoogleErrorMessage = ""
                        )
                    }
                }
        }
    }

    suspend fun getSignInIntentSender(): Either<Exception, IntentSender> =
        userRepository.getSignInIntentSender()

    fun onEvent(event: RegisterScreenEvent) {
        when (event) {
            is RegisterScreenEvent.OnEmailChange -> {
                _state.update { it.copy(email = event.email) }
            }

            is RegisterScreenEvent.OnIsConfirmPasswordVisibleChange -> {
                _state.update { it.copy(isConfirmPasswordVisible = event.isPasswordVisible) }
            }

            is RegisterScreenEvent.OnIsPasswordVisibleChange -> {
                _state.update { it.copy(isPasswordVisible = event.isPasswordVisible) }
            }

            is RegisterScreenEvent.OnPasswordChange -> {
                _state.update { it.copy(password = event.password) }
            }

            RegisterScreenEvent.OnRegisterClick -> {
                viewModelScope.launch {
                    userRepository.registerWithEmailAndPassword(
                        registerArg = RegisterWithUsernameEmailAndPassword(
                            username = _state.value.username,
                            email = _state.value.email,
                            password = _state.value.password
                        )
                    ).onLeft { exception ->
                        Timber.e(exception)
                        _state.update {
                            it.copy(
                                isRegisterSuccessful = false,
                                registerErrorMessage = exception.message.orEmpty()
                            )
                        }
                    }.onRight {
                        Timber.d("register successful")
                        _state.update { it.copy(isRegisterSuccessful = true) }
                    }
                }
            }

            is RegisterScreenEvent.OnConfirmPasswordChange -> {
                _state.update { it.copy(confirmPassword = event.confirmPassword) }
            }

            RegisterScreenEvent.OnRegisterWithGoogleClick -> {

            }

            is RegisterScreenEvent.OnUsernameChange -> {
                _state.update { it.copy(username = event.username) }
            }
        }
    }

}
