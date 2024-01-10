package com.onirutla.flexchat.ui.components

import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.onirutla.flexchat.R
import com.onirutla.flexchat.ui.theme.FlexChatTheme

@Composable
fun CaptionField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: KeyboardActionScope.() -> Unit,
) {
    OutlinedTextField(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        placeholder = { Text(text = stringResource(R.string.add_a_caption)) },
        value = value,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(onSend = onSend),
        onValueChange = onValueChange,
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun CaptionFieldPreview() {
    FlexChatTheme {
        CaptionField(value = "", onValueChange = {}, onSend = {})
    }
}
