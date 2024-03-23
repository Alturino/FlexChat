package com.onirutla.flexchat.splash_screen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.onirutla.flexchat.auth.ui.AuthActivity
import com.onirutla.flexchat.splash_screen.ui.SplashScreenViewModel
import com.onirutla.flexchat.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@AndroidEntryPoint
class SplashScreenActivity : ComponentActivity() {

    private val vm: SplashScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen().apply {
            setKeepOnScreenCondition { true }
        }

        vm.isLoggedIn.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { Timber.d("isLoggedIn: $it") }
            .onEach { isLoggedIn ->
                when (isLoggedIn) {
                    true -> {
                        Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }.also {
                            startActivity(it)
                            finishAndRemoveTask()
                        }
                    }

                    false -> {
                        Intent(this, AuthActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }.also {
                            startActivity(it)
                            finishAndRemoveTask()
                        }
                    }

                    null -> {}
                }
            }.launchIn(lifecycleScope)



    }
}

