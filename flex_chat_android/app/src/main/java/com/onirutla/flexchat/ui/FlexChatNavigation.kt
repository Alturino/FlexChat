/*
 * MIT License
 *
 * Copyright (c) 2023 Ricky Alturino
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.onirutla.flexchat.ui

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.onirutla.flexchat.ui.screens.Screens
import com.onirutla.flexchat.ui.screens.SharedViewModel
import com.onirutla.flexchat.ui.screens.add_new_conversation_screen.AddNewConversationScreen
import com.onirutla.flexchat.ui.screens.add_new_conversation_screen.AddNewConversationScreenUiEvent
import com.onirutla.flexchat.ui.screens.add_new_conversation_screen.AddNewConversationScreenViewModel
import com.onirutla.flexchat.ui.screens.conversation_screen.ConversationScreen
import com.onirutla.flexchat.ui.screens.conversation_screen.ConversationScreenUiEvent
import com.onirutla.flexchat.ui.screens.conversation_screen.ConversationScreenViewModel
import com.onirutla.flexchat.ui.screens.login_screen.LoginScreen
import com.onirutla.flexchat.ui.screens.login_screen.LoginScreenUiEvent
import com.onirutla.flexchat.ui.screens.login_screen.LoginScreenViewModel
import com.onirutla.flexchat.ui.screens.main_screen.MainScreen
import com.onirutla.flexchat.ui.screens.main_screen.MainScreenUiEvent
import com.onirutla.flexchat.ui.screens.main_screen.MainScreenViewModel
import com.onirutla.flexchat.ui.screens.profile_screen.ProfileScreen
import com.onirutla.flexchat.ui.screens.profile_screen.ProfileScreenUiEvent
import com.onirutla.flexchat.ui.screens.profile_screen.ProfileScreenViewModel
import com.onirutla.flexchat.ui.screens.register_screen.RegisterScreen
import com.onirutla.flexchat.ui.screens.register_screen.RegisterScreenUiEvent
import com.onirutla.flexchat.ui.screens.register_screen.RegisterScreenViewModel
import kotlinx.coroutines.launch

@Composable
fun FlexChatNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val sharedVm: SharedViewModel = hiltViewModel()
    val isLoggedIn by sharedVm.isLoggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = isLoggedIn, block = {
        if (isLoggedIn == false) {
            navController.navigate(route = Screens.LoginScreen.route) {
                launchSingleTop = true
                popUpTo(route = Screens.MainScreen.route) {
                    inclusive = true
                }
            }
        }
    })

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screens.MainScreen.route
    ) {
        composable(
            route = Screens.MainScreen.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) { backStackEntry ->
            val vm: MainScreenViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            MainScreen(
                state = state,
                onUiEvent = {
                    when (it) {
                        is MainScreenUiEvent.OnConversationClick -> {
                            navController.navigate(route = "${Screens.ConversationScreen.route}/${it.conversation.id}")
                        }

                        MainScreenUiEvent.OnFloatingActionButtonClick -> {
                            navController.navigate(Screens.AddNewConversationScreen.route)
                        }

                        MainScreenUiEvent.OnProfileIconClick -> {
                            navController.navigate(Screens.ProfileScreen.route)
                        }
                    }
                }
            )
        }
        composable(
            route = Screens.LoginScreen.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) {
            val vm: LoginScreenViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            val coroutineScope = rememberCoroutineScope()

            val signInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = {
                    if (it.resultCode == RESULT_OK) {
                        vm.loginWithGoogle(it.data ?: return@rememberLauncherForActivityResult)
                    }
                }
            )

            LoginScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = {
                    when (it) {
                        LoginScreenUiEvent.OnDontHaveAccountClick -> {
                            navController.navigate(route = Screens.RegisterScreen.route) {
                                launchSingleTop = true
                                popUpTo(route = Screens.LoginScreen.route) {
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
                                popUpTo(route = Screens.LoginScreen.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            )
        }
        composable(
            route = Screens.RegisterScreen.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) {
            val vm: RegisterScreenViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            RegisterScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = {
                    when (it) {
                        RegisterScreenUiEvent.OnHaveAnAccountClick -> {
                            navController.navigate(route = Screens.LoginScreen.route) {
                                launchSingleTop = true
                                popUpTo(route = Screens.RegisterScreen.route) {
                                    inclusive = true
                                }
                            }
                        }

                        RegisterScreenUiEvent.OnRegisterWithGoogleClick -> {

                        }

                        RegisterScreenUiEvent.NavigateToMainScreen -> {
                            navController.navigate(route = Screens.MainScreen.route) {
                                launchSingleTop = true
                                popUpTo(route = Screens.RegisterScreen.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            )
        }
        composable(
            route = Screens.AddNewConversationScreen.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) {
            val vm: AddNewConversationScreenViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            AddNewConversationScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = {
                    when (it) {
                        AddNewConversationScreenUiEvent.OnNavigateUpClick -> {
                            navController.navigateUp()
                        }

                        is AddNewConversationScreenUiEvent.OnNavigateToConversationScreen -> {
                            navController.navigate(route = "${Screens.ConversationScreen.route}/${it.conversationId}") {
                                popUpTo(route = Screens.AddNewConversationScreen.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            )
        }
        composable(
            route = Screens.ProfileScreen.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) {
            val vm: ProfileScreenViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            ProfileScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = {
                    when (it) {
                        ProfileScreenUiEvent.OnChangeProfilePhotoClick -> {

                        }

                        ProfileScreenUiEvent.OnChangeStatusClick -> {

                        }

                        ProfileScreenUiEvent.OnChangeUsernameClick -> {

                        }

                        ProfileScreenUiEvent.OnNavigateUpClick -> {
                            navController.navigateUp()
                        }
                    }
                }
            )
        }
        composable(
            route = "${Screens.ConversationScreen.route}/{conversationId}",
            arguments = listOf(
                navArgument(name = "conversationId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = false
                }
            ),
            deepLinks = listOf(),
        ) { backStackEntry ->
            val vm: ConversationScreenViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()


            val conversationId = backStackEntry.arguments?.getString("conversationId", "").orEmpty()
            LaunchedEffect(key1 = conversationId, block = {
                vm(conversationId)
            })

            ConversationScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = {
                    when (it) {
                        ConversationScreenUiEvent.OnNavigateUp -> {
                            navController.navigateUp()
                        }
                    }
                }
            )
        }
    }
}
