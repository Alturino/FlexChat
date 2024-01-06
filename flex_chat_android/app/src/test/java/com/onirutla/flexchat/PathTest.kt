package com.onirutla.flexchat

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class PathTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun pathTest() {
//        val app = RuntimeEnvironment.getApplication()
//        val outputDir = File(
//            app.getFileStreamPath(Environment.DIRECTORY_PICTURES),
//            app.applicationContext.getString(R.string.app_name)
//        ).apply { mkdirs() }
//        assertEquals("", outputDir.path)
    }
}
