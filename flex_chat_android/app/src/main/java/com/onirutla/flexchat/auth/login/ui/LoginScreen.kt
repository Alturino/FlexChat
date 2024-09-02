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

import android.content.res.Configuration
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.onirutla.flexchat.ui.components.GoogleIconButton
import com.onirutla.flexchat.ui.components.PasswordField
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    onUiEvent: (LoginUiEvent) -> Unit,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(
        key1 = state.isLoginSuccessful,
        key2 = state.isLoginWithGoogleSuccessful,
        block = {
            if (state.isLoginSuccessful == true) {
                onUiEvent(LoginUiEvent.NavigateToMain)
            }
            if (state.isLoginSuccessful == false) {
                snackBarHostState.showSnackbar(state.loginErrorMessage)
            }
            if (state.isLoginWithGoogleSuccessful == true) {
                onUiEvent(LoginUiEvent.NavigateToMain)
            }
            if (state.isLoginWithGoogleSuccessful == false) {
                snackBarHostState.showSnackbar(state.loginWithGoogleErrorMessage)
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
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) {
                Snackbar(snackbarData = it)
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f),
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
                        text = stringResource(R.string.login_to_start_chatting),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(unbounded = true)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                EmailField(
                    modifier = Modifier.fillMaxWidth(),
                    email = state.email,
                    isError = state.isEmailError ?: false,
                    onEmailChange = { onEvent(LoginEvent.OnEmailChange(it)) },
                    onNext = { defaultKeyboardAction(imeAction = ImeAction.Next) }
                )
                PasswordField(
                    modifier = Modifier.fillMaxWidth(),
                    password = state.password,
                    isError = state.isPasswordError ?: false,
                    onPasswordChange = { onEvent(LoginEvent.OnPasswordChange(it)) },
                    isPasswordVisible = state.isPasswordVisible,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onEvent(LoginEvent.OnLoginClicked)
                            defaultKeyboardAction(imeAction = ImeAction.Done)
                        },
                    ),
                    onPasswordVisibleChange = {
                        onEvent(LoginEvent.OnIsPasswordVisibleChange(it))
                    },
                )
                TextButton(onClick = { onUiEvent(LoginUiEvent.OnForgotPasswordClick) }) {
                    Text(text = stringResource(R.string.forgot_password))
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEvent(LoginEvent.OnLoginClicked) }
                ) {
                    Text(text = stringResource(R.string.login))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    GoogleIconButton(
                        modifier = Modifier,
                        iconSize = 35.dp,
                        onClick = { onUiEvent(LoginUiEvent.OnLoginWithGoogleClick) }
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
                TextButton(onClick = { onUiEvent(LoginUiEvent.OnDontHaveAccountClick) }) {
                    Text(text = stringResource(R.string.don_t_have_account_register))
                }
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = true,
    showBackground = true
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun LoginScreenPreview() {
    FlexChatTheme {
        LoginScreen(state = LoginState(), onEvent = {}, onUiEvent = {})
    }
}
