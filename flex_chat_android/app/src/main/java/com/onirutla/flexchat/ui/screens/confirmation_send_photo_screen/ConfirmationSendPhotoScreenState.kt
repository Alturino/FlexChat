package com.onirutla.flexchat.ui.screens.confirmation_send_photo_screen

import android.net.Uri

data class ConfirmationSendPhotoScreenState(
    val photoUri: Uri = Uri.EMPTY,
    val caption: String = "",
)
