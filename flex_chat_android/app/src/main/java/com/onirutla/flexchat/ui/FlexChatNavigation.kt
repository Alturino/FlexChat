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

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.onirutla.flexchat.ui.screens.authNavGraph
import com.onirutla.flexchat.ui.screens.conversation_screen.ConversationScreen
import com.onirutla.flexchat.ui.screens.conversation_screen.ConversationScreenUiEvent
import com.onirutla.flexchat.ui.screens.conversation_screen.ConversationScreenViewModel
import com.onirutla.flexchat.ui.screens.main_screen.MainScreen
import com.onirutla.flexchat.ui.screens.main_screen.MainScreenUiEvent
import com.onirutla.flexchat.ui.screens.main_screen.MainScreenViewModel
import com.onirutla.flexchat.ui.screens.profile_screen.ProfileScreen
import com.onirutla.flexchat.ui.screens.profile_screen.ProfileScreenUiEvent
import com.onirutla.flexchat.ui.screens.profile_screen.ProfileScreenViewModel
import timber.log.Timber

@Composable
fun FlexChatNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val sharedVm: SharedViewModel = hiltViewModel()
    val isLoggedIn by sharedVm.isLoggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = isLoggedIn, block = {
        if (isLoggedIn == false) {
            navController.navigate(route = Screens.Auth.Default.route) {
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
        authNavGraph(navController)
        composable(
            route = Screens.MainScreen.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) {
            val vm: MainScreenViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            val requestNotificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    Timber.d("Notification permission isGranted: $isGranted")
                } else {
                    Timber.e("Notification permission isNotGranted: ${!isGranted}")
                }
            }

            LaunchedEffect(key1 = Unit, block = {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return@LaunchedEffect
            })

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

                        ConversationScreenUiEvent.OnAttachmentClick -> {

                        }

                        ConversationScreenUiEvent.OnCameraClick -> {

                        }

                        ConversationScreenUiEvent.OnEmojiClick -> {

                        }
                    }
                }
            )
        }
    }
}
