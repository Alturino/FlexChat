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

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.onirutla.flexchat.auth.ui.MainActivityViewModel
import com.onirutla.flexchat.conversation.ui.call.CallActivity
import com.onirutla.flexchat.core.ui.InAppIncomingCallNotification
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vm: MainActivityViewModel by viewModels()
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen().apply {
            setKeepOnScreenCondition { true }
        }

        vm.isLoggedIn.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { isLoggedIn ->
                if (isLoggedIn != null) {
                    splashScreen.setKeepOnScreenCondition { false }
                }
            }
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .launchIn(lifecycleScope)

        setContent {
            val context = LocalContext.current
            navController = rememberNavController()
//            val incomingCall by vm.incomingCall.collectAsStateWithLifecycle()
//            var isIncomingCallVisible by remember { mutableStateOf(incomingCall != null) }
            FlexChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
//                        AnimatedVisibility(visible = isIncomingCallVisible) {
//                            InAppIncomingCallNotification(
//                                onAccept = {
//                                    Intent(context, CallActivity::class.java).apply {
//                                        putExtra("conversationId", incomingCall?.conversationId)
//                                        putExtra("callInitiatorId", incomingCall?.callInitiatorId)
//                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                    }.also {
//                                        context.startActivity(it)
//                                    }
//                                },
//                                onReject = {
//                                    isIncomingCallVisible = (incomingCall != null) or false
//                                }
//                            )
//                        }
                        FlexChatNavigation(
                            modifier = Modifier,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}


