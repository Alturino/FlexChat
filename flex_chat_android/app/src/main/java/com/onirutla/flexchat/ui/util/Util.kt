package com.onirutla.flexchat.ui.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun LocalDateTime.toLocalTimeString(): String {
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    return formatter.format(this)
}
