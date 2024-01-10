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

package com.onirutla.flexchat.ui.screens.register_screen

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
import com.onirutla.flexchat.ui.components.EmailField
import com.onirutla.flexchat.ui.components.GoogleIconButton
import com.onirutla.flexchat.ui.components.PasswordField
import com.onirutla.flexchat.ui.components.UsernameField
import com.onirutla.flexchat.ui.theme.FlexChatTheme

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    state: RegisterScreenState,
    onEvent: (RegisterScreenEvent) -> Unit,
    onUiEvent: (RegisterScreenUiEvent) -> Unit,
) {
    LaunchedEffect(key1 = state.isRegisterSuccessful, block = {
        if (state.isRegisterSuccessful == true) {
            onUiEvent(RegisterScreenUiEvent.NavigateToMainScreen)
        }
    })
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
