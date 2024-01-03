package com.onirutla.flexchat.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.onirutla.flexchat.ui.screens.login_screen.LoginScreen
import com.onirutla.flexchat.ui.screens.login_screen.LoginScreenUiEvent
import com.onirutla.flexchat.ui.screens.login_screen.LoginScreenViewModel
import com.onirutla.flexchat.ui.screens.register_screen.RegisterScreen
import com.onirutla.flexchat.ui.screens.register_screen.RegisterScreenUiEvent
import com.onirutla.flexchat.ui.screens.register_screen.RegisterScreenViewModel
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
            val vm: LoginScreenViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            val coroutineScope = rememberCoroutineScope()

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
                onUiEvent = {
                    when (it) {
                        LoginScreenUiEvent.OnDontHaveAccountClick -> {
                            navController.navigate(route = Screens.Auth.RegisterScreen.route) {
                                launchSingleTop = true
                                popUpTo(route = Screens.Auth.LoginScreen.route) {
                                    inclusive = true
                                }
                            }
                        }

                        LoginScreenUiEvent.OnForgotPasswordClick -> {

                        }

                        LoginScreenUiEvent.OnLoginWithGoogleClick -> {
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

                        LoginScreenUiEvent.NavigateToMainScreen -> {
                            navController.navigate(route = Screens.MainScreen.route) {
                                launchSingleTop = true
                                popUpTo(route = Screens.Auth.Default.route) {
                                    inclusive = true
                                }
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
                onUiEvent = {
                    when (it) {
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
                                if (intentSender.isRight()) {
                                    val intentSenderRequest =
                                        IntentSenderRequest.Builder(intentSender.getOrNull()!!)
                                            .build()
                                    registerWithGoogleLauncher.launch(intentSenderRequest)
                                }
                            }
                        }

                        RegisterScreenUiEvent.NavigateToMainScreen -> {
                            navController.navigate(route = Screens.MainScreen.route) {
                                launchSingleTop = true
                                popUpTo(route = Screens.Auth.Default.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            )
        }

    }
}
