package com.onirutla.flexchat.core.util

import androidx.core.util.PatternsCompat

fun String.isValidEmail() = (this.isNotBlank() or this.isNotEmpty()) and
    PatternsCompat.EMAIL_ADDRESS.matcher(this).matches()
