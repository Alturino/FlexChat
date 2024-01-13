/*
 * MIT License
 *
 * Copyright (c) 2023 - 2023 Ricky Alturino
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

package com.onirutla.flexchat.ui.screens.login_screen

import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.onirutla.flexchat.core.util.toEmailEither
import com.onirutla.flexchat.domain.repository.UserRepository
import com.onirutla.flexchat.domain.util.isValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginScreenState())
    val state = _state.asStateFlow()

    private val email = _state.mapLatest { it.email }
        .distinctUntilChanged()
        .onEach { Timber.d("email: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val emailError = email.mapLatest { it.toEmailEither() }
        .onEach { Timber.d("emailError: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val isEmailError = emailError.mapLatest { it.isLeft() }
        .distinctUntilChanged()
        .onEach { Timber.d("isEmailError: $it") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val emailErrorMessage = emailError
        .mapLatest { emailError -> emailError.mapLeft { it.message }.leftOrNull() }
        .distinctUntilChanged()
        .onEach { Timber.d("emailErrorMessage: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val password = _state.mapLatest { it.password }
        .distinctUntilChanged()
        .onEach { Timber.d("password: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val passwordError = password.mapLatest { it.isValidPassword() }
        .distinctUntilChanged()
        .onEach { Timber.d("passwordError: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val isPasswordError = passwordError.mapLatest { it.isLeft() }
        .distinctUntilChanged()
        .onEach { Timber.d("isPasswordError: $it") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val passwordErrorMessage = passwordError
        .mapLatest { passwordError -> passwordError.mapLeft { it.message }.leftOrNull() }
        .distinctUntilChanged()
        .onEach { Timber.d("passwordErrorMessage: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )


    init {
        viewModelScope.launch {
            launch {
                isEmailError.collect { isEmailError ->
                    _state.update { it.copy(isEmailError = isEmailError) }
                }
            }
            launch {
                emailErrorMessage.collect { errorMessage ->
                    _state.update { it.copy(emailErrorMessage = errorMessage.orEmpty()) }
                }
            }
            launch {
                isPasswordError.collect { isPasswordError ->
                    _state.update { it.copy(isPasswordError = isPasswordError) }
                }
            }
            launch {
                passwordErrorMessage.collect { errorMessage ->
                    _state.update { it.copy(passwordErrorMessage = errorMessage.orEmpty()) }
                }
            }
        }
    }

    fun onEvent(event: LoginScreenEvent) {
        when (event) {
            is LoginScreenEvent.OnEmailChange -> {
                _state.update { it.copy(email = event.email) }
            }

            is LoginScreenEvent.OnIsPasswordVisibleChange -> {
                _state.update { it.copy(isPasswordVisible = event.isPasswordVisible) }
            }

            LoginScreenEvent.OnLoginClicked -> {
                viewModelScope.launch {
                    userRepository.login(
                        email = _state.value.email,
                        password = _state.value.password
                    ).onLeft { exception ->
                        Timber.e(exception)
                        _state.update { it.copy(isLoginSuccessful = false) }
                    }.onRight { unit ->
                        Timber.d("$unit")
                        _state.update { it.copy(isLoginSuccessful = true) }
                    }
                }
            }

            is LoginScreenEvent.OnPasswordChange -> {
                _state.update { it.copy(password = event.password) }
            }
        }
    }

    fun loginWithGoogle(intent: Intent) {
        viewModelScope.launch {
            userRepository.loginWithGoogle(intent)
                .onLeft { exception ->
                    Timber.e("loginFailed with exception: $exception")
                    _state.update {
                        it.copy(
                            isLoginWithGoogleSuccessful = false,
                            loginWithGoogleErrorMessage = exception.localizedMessage.orEmpty()
                        )
                    }
                }
                .onRight { user ->
                    Timber.d("loginSuccessful: $user")
                    _state.update {
                        it.copy(
                            isLoginWithGoogleSuccessful = true,
                            loginWithGoogleErrorMessage = ""
                        )
                    }
                }
        }
    }

    suspend fun getSignInIntentSender(): Either<Throwable, IntentSender> =
        userRepository.getSignInIntentSender()

}
