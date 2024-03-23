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

package com.onirutla.flexchat.auth.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.onirutla.flexchat.auth.login.ui.LoginScreen
import com.onirutla.flexchat.auth.login.ui.LoginUiEvent
import com.onirutla.flexchat.auth.login.ui.LoginViewModel
import com.onirutla.flexchat.auth.register.ui.RegisterScreen
import com.onirutla.flexchat.auth.register.ui.RegisterScreenUiEvent
import com.onirutla.flexchat.auth.register.ui.RegisterScreenViewModel
import com.onirutla.flexchat.core.ui.Screens
import com.onirutla.flexchat.ui.MainActivity
import kotlinx.coroutines.launch
import timber.log.Timber

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Screens.Auth.LoginScreen.route,
        route = Screens.Auth.Default.route
    ) {
        composable(
            route = Screens.Auth.LoginScreen.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) {
            val vm: LoginViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current

            val signInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    vm.loginWithGoogle(it.data ?: return@rememberLauncherForActivityResult)
                } else {
                    Timber.e("Login failed with resultCode: ${it.resultCode}")
                }
            }

            LoginScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = { uiEvent ->
                    when (uiEvent) {
                        LoginUiEvent.OnDontHaveAccountClick -> {
                            navController.navigate(route = Screens.Auth.RegisterScreen.route) {
                                launchSingleTop = true
                                popUpTo(route = Screens.Auth.LoginScreen.route) {
                                    inclusive = true
                                }
                            }
                        }

                        LoginUiEvent.OnForgotPasswordClick -> {
                            // TODO: Implement forgot password
                        }

                        LoginUiEvent.OnLoginWithGoogleClick -> {
                            coroutineScope.launch {
                                val intentSender = vm.getSignInIntentSender()
                                if (intentSender.isRight()) {
                                    val intentSenderRequest =
                                        IntentSenderRequest.Builder(intentSender.getOrNull()!!)
                                            .build()
                                    signInLauncher.launch(intentSenderRequest)
                                }
                            }
                        }

                        LoginUiEvent.NavigateToMain -> {
                            Intent(context, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }.also {
                                context.startActivity(it)
                            }
                        }
                    }
                }
            )
        }

        composable(
            route = Screens.Auth.RegisterScreen.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) {
            val vm: RegisterScreenViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current

            val registerWithGoogleLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = {
                    if (it.resultCode == Activity.RESULT_OK) {
                        vm.registerWithGoogle(it.data ?: return@rememberLauncherForActivityResult)
                    } else {
                        Timber.e("Register with google failed with resultCode: ${it.resultCode}")
                    }
                }
            )

            RegisterScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = { uiEvent ->
                    when (uiEvent) {
                        RegisterScreenUiEvent.OnHaveAnAccountClick -> {
                            navController.navigate(route = Screens.Auth.LoginScreen.route) {
                                launchSingleTop = true
                                popUpTo(route = Screens.Auth.RegisterScreen.route) {
                                    inclusive = true
                                }
                            }
                        }

                        RegisterScreenUiEvent.OnRegisterWithGoogleClick -> {
                            coroutineScope.launch {
                                val intentSender = vm.getSignInIntentSender()
                                    .onLeft { Timber.e(it) }
                                if (intentSender.isRight()) {
                                    val intentSenderRequest =
                                        IntentSenderRequest.Builder(intentSender.getOrNull()!!)
                                            .build()
                                    registerWithGoogleLauncher.launch(intentSenderRequest)
                                }
                            }
                        }

                        RegisterScreenUiEvent.NavigateToMainScreen -> {
                            Intent(context, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }.also {
                                context.startActivity(it)
                            }
                        }
                    }
                }
            )
        }

    }
}
