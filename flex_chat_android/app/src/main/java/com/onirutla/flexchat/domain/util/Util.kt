package com.onirutla.flexchat.domain.util

fun String.isValidPassword() = (this.isNotBlank() or this.isNotEmpty()) and
    (this.length >= 8) and
    (this.firstOrNull { it.isDigit() } != null) and
    (this.firstOrNull { it.isLetter() } != null) and
    (this.firstOrNull { it.isLetter() }?.isLowerCase() ?: false) and
    (this.firstOrNull { it.isLetter() }?.isUpperCase() ?: false) and
    (this.firstOrNull { !it.isLetterOrDigit() } != null)

