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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onirutla.flexchat.R
import com.onirutla.flexchat.core.ui.components.EmailField
import com.onirutla.flexchat.core.ui.components.UsernameField
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme
import com.onirutla.flexchat.ui.components.GoogleIconButton
import com.onirutla.flexchat.ui.components.PasswordField

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    state: RegisterScreenState,
    onEvent: (RegisterScreenEvent) -> Unit,
    onUiEvent: (RegisterScreenUiEvent) -> Unit,
) {
    LaunchedEffect(state.isRegisterSuccessful) {
        if (state.isRegisterSuccessful == true) {
            onUiEvent(RegisterScreenUiEvent.NavigateToMainScreen)
        }
    }
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .navigationBarsPadding(),
        contentWindowInsets = WindowInsets(
            left = 16.dp,
            top = 16.dp,
            right = 16.dp,
            bottom = 16.dp
        ),
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.welcome_to,
                            stringResource(id = R.string.app_name)
                        ),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = stringResource(R.string.register_to_start_chatting),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                UsernameField(
                    modifier = Modifier.fillMaxWidth(),
                    username = state.username,
                    onUsernameChange = { onEvent(RegisterScreenEvent.OnUsernameChange(it)) },
                    onNext = { defaultKeyboardAction(imeAction = ImeAction.Next) }
                )
                EmailField(
                    modifier = Modifier.fillMaxWidth(),
                    email = state.email,
                    isError = state.isEmailError == true,
                    errorMessage = state.emailErrorMessage,
                    onEmailChange = { onEvent(RegisterScreenEvent.OnEmailChange(it)) },
                    onNext = { defaultKeyboardAction(imeAction = ImeAction.Next) }
                )
                PasswordField(
                    modifier = Modifier.fillMaxWidth(),
                    password = state.password,
                    isError = state.isPasswordError == true,
                    errorMessage = state.passwordErrorMessage,
                    onPasswordChange = { onEvent(RegisterScreenEvent.OnPasswordChange(it)) },
                    isPasswordVisible = state.isPasswordVisible,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { defaultKeyboardAction(imeAction = ImeAction.Next) }),
                    onPasswordVisibleChange = {
                        onEvent(RegisterScreenEvent.OnIsPasswordVisibleChange(it))
                    },
                )
                PasswordField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.confirm_password),
                    password = state.confirmPassword,
                    isError = state.isConfirmPasswordError == true,
                    errorMessage = state.confirmPasswordErrorMessage,
                    onPasswordChange = { onEvent(RegisterScreenEvent.OnConfirmPasswordChange(it)) },
                    isPasswordVisible = state.isConfirmPasswordVisible,
                    onPasswordVisibleChange = {
                        onEvent(RegisterScreenEvent.OnIsConfirmPasswordVisibleChange(it))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            defaultKeyboardAction(imeAction = ImeAction.Done)
                            onEvent(RegisterScreenEvent.OnRegisterClick)
                        },
                    ),
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.isRegisterButtonEnabled == true,
                    onClick = { onEvent(RegisterScreenEvent.OnRegisterClick) }
                ) {
                    Text(text = stringResource(R.string.register))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    GoogleIconButton(
                        modifier = Modifier,
                        iconSize = 35.dp,
                        onClick = { onUiEvent(RegisterScreenUiEvent.OnRegisterWithGoogleClick) }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = { onUiEvent(RegisterScreenUiEvent.OnHaveAnAccountClick) }) {
                    Text(text = stringResource(R.string.have_an_account_login))
                }
            }
        }
    }
}

@Preview
@Composable
private fun RegisterScreenPreview() {
    FlexChatTheme {
        val isEmailError = false
        val password = "graece"
        val confirmPassword = "graece"
        val isPasswordError = false
        val isConfirmPasswordError = false
        val isPasswordSameAsConfirmPassword = password == confirmPassword
        RegisterScreen(
            state = RegisterScreenState(
                email = "frances.randall@example.com",
                isEmailError = isEmailError,
                password = password,
                isPasswordError = isPasswordError,
                isPasswordVisible = false,
                confirmPassword = confirmPassword,
                isRegisterButtonEnabled = !isEmailError and !isPasswordError and !isConfirmPasswordError and isPasswordSameAsConfirmPassword,
                isConfirmPasswordError = isConfirmPasswordError,
                isConfirmPasswordVisible = false,
                isRegisterSuccessful = false,
                registerErrorMessage = "epicuri",
                isRegisterWithGoogleSuccessful = null,
                registerWithGoogleErrorMessage = "mollis"
            ),
            onEvent = {},
            onUiEvent = {}
        )
    }
}
