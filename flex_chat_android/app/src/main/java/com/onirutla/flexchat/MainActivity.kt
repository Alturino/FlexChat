package com.onirutla.flexchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.onirutla.flexchat.auth.ui.AuthViewModel
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authVm: AuthViewModel by viewModels()
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen().apply {
            setKeepOnScreenCondition { true }
        }

        authVm.isLoggedIn.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { isLoggedIn ->
                if (isLoggedIn != null) {
                    splashScreen.setKeepOnScreenCondition { false }
                }
            }
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .launchIn(lifecycleScope)

        setContent {
            navController = rememberNavController()
            FlexChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FlexChatNavigation(
                        modifier = Modifier,
                        navController = navController
                    )
                }
            }
        }
    }
}


