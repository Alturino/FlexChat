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

package com.onirutla.flexchat.auth.register.ui

import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.onirutla.flexchat.auth.domain.model.PasswordError
import com.onirutla.flexchat.auth.domain.repository.AuthRepository
import com.onirutla.flexchat.auth.domain.usecase.EmailValidationUseCase
import com.onirutla.flexchat.auth.domain.usecase.PasswordValidationUseCase
import com.onirutla.flexchat.auth.register.domain.data.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RegisterScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterScreenState())
    val state = _state.asStateFlow()

    private val email = _state
        .mapLatest { it.email }
        .distinctUntilChanged()
        .onEach { Timber.d("email: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val password = _state
        .mapLatest { it.password }
        .distinctUntilChanged()
        .onEach { Timber.d("password: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
        )

    private val confirmPassword = _state
        .mapLatest { it.confirmPassword }
        .distinctUntilChanged()
        .onEach { Timber.d("confirmPassword: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
        )

    private val emailError = email
        .mapLatest { EmailValidationUseCase(it) }
        .distinctUntilChanged()
        .onEach { Timber.d("emailError: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val isEmailError = emailError
        .mapLatest { it.isLeft() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val emailErrorMessage = emailError
        .mapLatest { emailError -> emailError.mapLeft { it.message }.leftOrNull() }
        .onEach { Timber.d("emailErrorMessage: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val isPasswordSameAsConfirmPassword = combine(
        flow = password,
        flow2 = confirmPassword
    ) { password, confirmPassword ->
        either {
            ensure(password == confirmPassword) {
                raise(PasswordError.PasswordNotTheSameWithConfirmationPassword)
            }
            password
        }
    }.distinctUntilChanged()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val isConfirmPasswordSameAsPassword = combine(
        flow = confirmPassword,
        flow2 = password
    ) { confirmPassword, password ->
        either {
            ensure(confirmPassword == password) {
                raise(PasswordError.ConfirmationPasswordNotTheSameWithPassword)
            }
            confirmPassword
        }
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly
    )

    private val passwordError = merge(
        password.mapLatest { PasswordValidationUseCase(it) },
        isPasswordSameAsConfirmPassword
    ).distinctUntilChanged()
        .onEach { Timber.d("passwordError: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val passwordErrorMessage = passwordError
        .mapLatest { passwordError -> passwordError.mapLeft { it.message }.leftOrNull() }
        .distinctUntilChanged()
        .onEach { Timber.d("passwordErrorMessage: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
        )

    private val isPasswordError = passwordError
        .mapLatest { it.isLeft() }
        .onEach { Timber.d("isPasswordError: $it") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    private val confirmPasswordError = merge(
        confirmPassword.mapLatest { PasswordValidationUseCase(it) },
        isConfirmPasswordSameAsPassword,
    ).distinctUntilChanged()
        .onEach { Timber.d("confirmPasswordError: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
        )

    private val isConfirmPasswordError = confirmPasswordError
        .mapLatest { it.isLeft() }
        .distinctUntilChanged()
        .onEach { Timber.d("isConfirmPasswordError: $it") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val confirmPasswordErrorMessage = confirmPasswordError
        .mapLatest { confirmPasswordError ->
            confirmPasswordError.mapLeft { it.message }.leftOrNull()
        }
        .distinctUntilChanged()
        .onEach { Timber.d("confirmPasswordErrorMessage: $it") }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        )

    private val isRegisterButtonEnabled = combine(
        isEmailError,
        isPasswordError,
        isConfirmPasswordError,
    ) { isEmailError, isPasswordError, isConfirmPasswordError ->
        (isEmailError == false) && (isPasswordError == false) && (isConfirmPasswordError == false)
    }.distinctUntilChanged()
        .onEach { Timber.d("isRegisterButtonEnabled: $it") }
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
            launch {
                isConfirmPasswordError.collect { isConfirmPasswordError ->
                    _state.update { it.copy(isConfirmPasswordError = isConfirmPasswordError) }
                }
            }
            launch {
                confirmPasswordErrorMessage.collect { errorMessage ->
                    _state.update { it.copy(confirmPasswordErrorMessage = errorMessage.orEmpty()) }
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
            authRepository.loginWithGoogle(intent)
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

//    suspend fun getSignInIntentSender(): Either<Throwable, IntentSender> =
//        userRepository.getSignInIntentSender()

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
                    authRepository.registerWithEmailAndPassword(
                        request = RegisterRequest(
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

    suspend fun getSignInIntentSender(): Either<Throwable, IntentSender> =
        authRepository.getSignInIntentSender()
            .onLeft { Timber.e(it) }
}
