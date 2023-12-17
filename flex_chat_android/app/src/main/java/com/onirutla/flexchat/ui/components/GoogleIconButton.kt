package com.onirutla.flexchat.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.onirutla.flexchat.R
import com.onirutla.flexchat.ui.theme.FlexChatTheme

@Composable
fun GoogleIconButton(
    modifier: Modifier = Modifier,
    iconSize: Dp,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.android_dark_rd_na else R.drawable.android_neutral_rd_na),
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun GoogleIconButtonPreview() {
    FlexChatTheme {
        GoogleIconButton(onClick = {}, iconSize = 75.dp)
    }
}
