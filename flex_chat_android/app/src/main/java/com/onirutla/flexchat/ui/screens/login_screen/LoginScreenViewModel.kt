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
import com.onirutla.flexchat.core.util.isValidEmail
import com.onirutla.flexchat.domain.repository.UserRepository
import com.onirutla.flexchat.domain.util.isValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: LoginScreenEvent) {
        when (event) {
            is LoginScreenEvent.OnEmailChange -> {
                _state.update {
                    it.copy(
                        email = event.email,
                        isEmailError = event.email.isValidEmail()
                    )
                }
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
                _state.update {
                    it.copy(
                        password = event.password,
                        isEmailError = event.password.isValidPassword()
                    )
                }
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

    suspend fun getSignInIntentSender(): Either<Exception, IntentSender> =
        userRepository.getSignInIntentSender()

}
