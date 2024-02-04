package com.onirutla.flexchat.ui.screens.confirmation_send_photo_screen

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.onirutla.flexchat.R
import com.onirutla.flexchat.ui.components.CaptionField
import com.onirutla.flexchat.ui.components.OnImageIconButton
import com.onirutla.flexchat.ui.theme.FlexChatTheme

sealed interface ConfirmationSendPhotoScreenUiEvent {
    data object OnClearClick : ConfirmationSendPhotoScreenUiEvent
}

sealed interface ConfirmationSendPhotoScreenEvent {
    data object OnSendClick : ConfirmationSendPhotoScreenEvent
    data class OnCaptionChange(val caption: String) : ConfirmationSendPhotoScreenEvent
}

@Composable
fun ConfirmationSendPhotoScreen(
    modifier: Modifier = Modifier,
    state: ConfirmationSendPhotoScreenState,
    onEvent: (ConfirmationSendPhotoScreenEvent) -> Unit,
    onUiEvent: (ConfirmationSendPhotoScreenUiEvent) -> Unit,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { contentPadding ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            val (topIcons, imagePreview, bottomCaption) = createRefs()
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.8f)
                    .constrainAs(ref = imagePreview) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    },
                model = state.photoUri,
                placeholder = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.High,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(topIcons) {
                        width = Dimension.fillToConstraints
                        start.linkTo(imagePreview.start)
                        top.linkTo(imagePreview.top)
                        end.linkTo(imagePreview.end)
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OnImageIconButton(onClick = { onUiEvent(ConfirmationSendPhotoScreenUiEvent.OnClearClick) }) {
                    Icon(imageVector = Icons.Rounded.Clear, contentDescription = null)
                }
                // TODO: Implement editing photo like crop, add text, and draw on the image
                Row {

                }
            }
            Row(
                modifier = Modifier
                    .constrainAs(bottomCaption) {
                        width = Dimension.fillToConstraints
                        height = Dimension.preferredWrapContent
                        start.linkTo(imagePreview.start)
                        end.linkTo(imagePreview.end)
                        top.linkTo(imagePreview.bottom)
                        bottom.linkTo(imagePreview.bottom)
                    }
                    .height(IntrinsicSize.Max)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CaptionField(
                    modifier = Modifier.fillMaxWidth(fraction = 0.85f),
                    value = state.caption,
                    onValueChange = { onEvent(ConfirmationSendPhotoScreenEvent.OnCaptionChange(it)) },
                    onSend = { onEvent(ConfirmationSendPhotoScreenEvent.OnSendClick) }
                )
                FilledTonalIconButton(onClick = { onEvent(ConfirmationSendPhotoScreenEvent.OnSendClick) }) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EditPhotoScreenPreview() {
    FlexChatTheme {
        ConfirmationSendPhotoScreen(
            state = ConfirmationSendPhotoScreenState(photoUri = Uri.EMPTY),
            onEvent = {},
            onUiEvent = {}
        )
    }
}
