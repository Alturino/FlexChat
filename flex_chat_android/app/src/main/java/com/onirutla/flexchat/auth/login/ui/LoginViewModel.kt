/*
 * Copyright 2024 Ricky Alturino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onirutla.flexchat.auth.login.ui

import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.onirutla.flexchat.auth.domain.repository.AuthRepository
import com.onirutla.flexchat.auth.domain.usecase.EmailValidationUseCase
import com.onirutla.flexchat.auth.domain.usecase.PasswordValidationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val email = _state.map { it.email }
        .distinctUntilChanged()
        .onEach { Timber.d("email: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val emailError = email.map { EmailValidationUseCase(it) }
        .onEach { Timber.d("emailError: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val isEmailError = emailError.map { it.isLeft() }
        .distinctUntilChanged()
        .onEach { Timber.d("isEmailError: $it") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val emailErrorMessage = emailError
        .map { emailError -> emailError.mapLeft { it.message }.leftOrNull() }
        .distinctUntilChanged()
        .onEach { Timber.d("emailErrorMessage: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val password = _state.map { it.password }
        .distinctUntilChanged()
        .onEach { Timber.d("password: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val passwordError = password.map { PasswordValidationUseCase(it) }
        .distinctUntilChanged()
        .onEach { Timber.d("passwordError: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val isPasswordError = passwordError.map { it.isLeft() }
        .distinctUntilChanged()
        .onEach { Timber.d("isPasswordError: $it") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val passwordErrorMessage = passwordError
        .map { passwordError -> passwordError.mapLeft { it.message }.leftOrNull() }
        .distinctUntilChanged()
        .onEach { Timber.d("passwordErrorMessage: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )


    init {
        isEmailError.onEach { isEmailError ->
            _state.update { it.copy(isEmailError = isEmailError) }
        }.launchIn(viewModelScope)

        emailErrorMessage.onEach { emailErrorMessage ->
            _state.update { it.copy(emailErrorMessage = emailErrorMessage.orEmpty()) }
        }.launchIn(viewModelScope)

        isPasswordError.onEach { isPasswordError ->
            _state.update { it.copy(isPasswordError = isPasswordError) }
        }.launchIn(viewModelScope)

        passwordErrorMessage.onEach { errorMessage ->
            _state.update { it.copy(passwordErrorMessage = errorMessage.orEmpty()) }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnEmailChange -> {
                _state.update { it.copy(email = event.email) }
            }

            is LoginEvent.OnIsPasswordVisibleChange -> {
                _state.update { it.copy(isPasswordVisible = event.isPasswordVisible) }
            }

            LoginEvent.OnLoginClicked -> {
                viewModelScope.launch {
                    authRepository.loginWithEmailAndPassword(
                        email = _state.value.email,
                        password = _state.value.password
                    ).onLeft { exception ->
                        Timber.e(exception)
                        _state.update { it.copy(isLoginSuccessful = false) }
                    }.onRight { user ->
                        Timber.d("$user")
                        _state.update { it.copy(isLoginSuccessful = true) }
                    }
                }
            }

            is LoginEvent.OnPasswordChange -> {
                _state.update { it.copy(password = event.password) }
            }
        }
    }

    fun loginWithGoogle(intent: Intent) {
        viewModelScope.launch {
            authRepository.loginWithGoogle(intent)
                .onLeft { exception ->
                    Timber.e("loginFailed with exception: $exception")
                    _state.update {
                        it.copy(
                            isLoginWithGoogleSuccessful = false,
                            loginWithGoogleErrorMessage = exception.message.orEmpty()
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
        authRepository.getSignInIntentSender()
            .onLeft { Timber.e(it) }

}
