package com.onirutla.flexchat.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.onirutla.flexchat.R
import com.onirutla.flexchat.ui.theme.FlexChatTheme

@Composable
fun PasswordField(
    modifier: Modifier = Modifier,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibleChange: (Boolean) -> Unit,
    label: String = stringResource(id = R.string.password),
    password: String,
    isError: Boolean = false,
    isPasswordVisible: Boolean,
    errorMessage: String = "",
    keyboardActions: KeyboardActions,
    keyboardOptions: KeyboardOptions,
) {
    OutlinedTextField(
        modifier = modifier,
        value = password,
        onValueChange = onPasswordChange,
        isError = isError,
        supportingText = {
            if (isError) {
                Text(text = errorMessage)
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        label = { Text(text = label) },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Password,
                contentDescription = stringResource(id = R.string.password),
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { onPasswordVisibleChange(!isPasswordVisible) },
                content = {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = null,
                    )
                }
            )
        }
    )
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true
)
@Composable
private fun PasswordFieldPreview() {
    FlexChatTheme {
        PasswordField(
            isPasswordVisible = true,
            keyboardOptions = KeyboardOptions(),
            keyboardActions = KeyboardActions(),
            onPasswordChange = {},
            onPasswordVisibleChange = {},
            password = "asdfasdf",
            errorMessage = "you're so wrong",
            isError = true,
        )
    }
}
