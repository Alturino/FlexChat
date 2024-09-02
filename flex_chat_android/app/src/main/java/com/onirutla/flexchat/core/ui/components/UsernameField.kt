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

package com.onirutla.flexchat.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
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
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme

@Composable
fun UsernameField(
    modifier: Modifier = Modifier,
    username: String,
    onUsernameChange: (String) -> Unit,
    onNext: KeyboardActionScope.() -> Unit,
) {
    OutlinedTextField(
        modifier = modifier,
        value = username,
        onValueChange = onUsernameChange,
        singleLine = true,
        supportingText = {},
        shape = MaterialTheme.shapes.medium,
        label = { Text(text = stringResource(R.string.username)) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = onNext),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
            )
        }
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true
)
@Composable
private fun UsernameFieldPreview() {
    FlexChatTheme {
        UsernameField(username = "user1", onUsernameChange = {}) {

        }
    }
}
