package com.onirutla.flexchat.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.onirutla.flexchat.R

@Composable
fun EmailField(
    modifier: Modifier = Modifier,
    email: String,
    onEmailChange: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String = "",
    onNext: KeyboardActionScope.() -> Unit,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = email,
        isError = isError,
        supportingText = {
            if (isError) {
                Text(text = errorMessage)
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        onValueChange = onEmailChange,
        label = { Text(text = stringResource(R.string.email)) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = onNext),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Email,
                contentDescription = null,
            )
        }
    )
}

@Preview
@Composable
private fun EmailFieldPreview() {

}
