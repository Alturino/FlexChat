package com.onirutla.flexchat.ui.screens.login_screen

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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onirutla.flexchat.R
import com.onirutla.flexchat.ui.components.EmailField
import com.onirutla.flexchat.ui.components.GoogleIconButton
import com.onirutla.flexchat.ui.components.PasswordField
import com.onirutla.flexchat.ui.theme.FlexChatTheme

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    state: LoginScreenState,
    onEvent: (LoginScreenEvent) -> Unit,
    onUiEvent: (LoginScreenUiEvent) -> Unit,
) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                )
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(unbounded = true)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                EmailField(
                    email = state.email,
                    isError = state.isEmailError ?: false,
                    onEmailChange = { onEvent(LoginScreenEvent.OnEmailChange(it)) },
                    onNext = { defaultKeyboardAction(imeAction = ImeAction.Next) }
                )
                PasswordField(
                    modifier = Modifier.fillMaxWidth(),
                    password = state.password,
                    isError = state.isPasswordError ?: false,
                    onPasswordChange = { onEvent(LoginScreenEvent.OnPasswordChange(it)) },
                    isPasswordVisible = state.isPasswordVisible,
                    onPasswordVisibleChange = {
                        onEvent(LoginScreenEvent.OnIsPasswordVisibleChange(it))
                    },
                    onDone = {
                        onEvent(LoginScreenEvent.OnLoginClicked)
                        defaultKeyboardAction(imeAction = ImeAction.Done)
                    },
                )
                TextButton(onClick = { onUiEvent(LoginScreenUiEvent.OnForgotPasswordClick) }) {
                    Text(text = stringResource(R.string.forgot_password))
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEvent(LoginScreenEvent.OnLoginClicked) },
                    content = { Text(text = stringResource(R.string.login)) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    GoogleIconButton(modifier = Modifier, iconSize = 35.dp, onClick = {})
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = {}) {
                    Text(text = stringResource(R.string.don_t_have_account_register))
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun LoginScreenPreview() {
    FlexChatTheme {
        LoginScreen(state = LoginScreenState(), onEvent = {}, onUiEvent = {})
    }
}