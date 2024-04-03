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

package com.onirutla.flexchat

import com.onirutla.flexchat.core.util.toLocalTimeString
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
