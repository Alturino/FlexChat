package com.onirutla.flexchat

import com.onirutla.flexchat.ui.util.toLocalTimeString
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class DateFormatterTest {
    @Test
    fun isValid() {
        val time = Clock.System.now().toJavaInstant()
        val expectedFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        val expectedLocalTime = LocalDateTime.ofInstant(time, ZoneId.systemDefault())
        val expected = expectedFormatter.format(expectedLocalTime)

        val actualTime = LocalDateTime.ofInstant(time, ZoneId.systemDefault())
        val actual = actualTime.toLocalTimeString()

        assertEquals(expected, actual)
    }

    @Test
    fun sorted() {
        print(listOf(LocalDateTime.MAX, LocalDateTime.MIN).sortedDescending())
    }

}
