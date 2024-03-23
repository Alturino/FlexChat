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

package com.onirutla.flexchat.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme

@Composable
fun OnImageIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    OutlinedIconButton(
        modifier = modifier,
        onClick = onClick,
        colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = Color.Transparent),
        content = content,
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CameraIconButtonPreview() {
    FlexChatTheme {
        OnImageIconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Rounded.Camera, contentDescription = null)
        }
    }
}
