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
import com.onirutla.flexchat.ui.theme.FlexChatTheme

@Composable
fun CameraIconButton(
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
        CameraIconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Rounded.Camera, contentDescription = null)
        }
    }
}
