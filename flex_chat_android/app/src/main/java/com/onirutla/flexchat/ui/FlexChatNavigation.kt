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
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.onirutla.flexchat.ui.screens.confirmation_send_photo_screen.ConfirmationSendPhotoScreen
import com.onirutla.flexchat.ui.screens.Screens
import com.onirutla.flexchat.ui.screens.SharedViewModel
import com.onirutla.flexchat.ui.screens.add_new_conversation_screen.AddNewConversationScreen
import com.onirutla.flexchat.ui.screens.add_new_conversation_screen.AddNewConversationScreenUiEvent
import com.onirutla.flexchat.ui.screens.add_new_conversation_screen.AddNewConversationScreenViewModel
import com.onirutla.flexchat.ui.screens.authNavGraph
import com.onirutla.flexchat.ui.screens.camera_screen.CameraScreen
import com.onirutla.flexchat.ui.screens.conversation_screen.ConversationScreen
import com.onirutla.flexchat.ui.screens.conversation_screen.ConversationScreenUiEvent
import com.onirutla.flexchat.ui.screens.conversation_screen.ConversationScreenViewModel
import com.onirutla.flexchat.ui.screens.main_screen.MainScreen
import com.onirutla.flexchat.ui.screens.main_screen.MainScreenUiEvent
import com.onirutla.flexchat.ui.screens.main_screen.MainScreenViewModel
import com.onirutla.flexchat.ui.screens.profile_screen.ProfileScreen
import com.onirutla.flexchat.ui.screens.profile_screen.ProfileScreenUiEvent
import com.onirutla.flexchat.ui.screens.profile_screen.ProfileScreenViewModel

@OptIn(ExperimentalPermissionsApi::class)
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

            val notificationPermissionState =
                rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
            if (!notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }

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
                            // TODO: Implement change profile picture
                        }

                        ProfileScreenUiEvent.OnChangeStatusClick -> {
                            // TODO: Implement change status
                        }

                        ProfileScreenUiEvent.OnChangeUsernameClick -> {
                            // TODO: Implement change username
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
            LaunchedEffect(key1 = conversationId, block = { vm(conversationId) })

            val cameraPermissionState = rememberPermissionState(
                permission = Manifest.permission.CAMERA,
                onPermissionResult = {
                    if (it) {
                        navController.navigate(route = Screens.CameraScreen.route)
                    }
                }
            )

            ConversationScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = {
                    when (it) {
                        ConversationScreenUiEvent.OnNavigateUp -> {
                            navController.navigateUp()
                        }

                        // TODO: Implement voice and or video chat

                        ConversationScreenUiEvent.OnAttachmentClick -> {
                            // TODO: Implement add blob to the chat
                        }

                        ConversationScreenUiEvent.OnCameraClick -> {
                            if (!cameraPermissionState.status.isGranted) {
                                cameraPermissionState.launchPermissionRequest()
                            } else {
                                navController.navigate(route = Screens.CameraScreen.route)
                            }
                        }

                        ConversationScreenUiEvent.OnEmojiClick -> {
                            // TODO: Implement emoji to the chat
                        }
                    }
                }
            )
        }
        composable(route = Screens.CameraScreen.route) {
            CameraScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToEditPhotoScreen = {
                    navController.navigate(route = "${Screens.EditPhotoScreen.route}/${Uri.encode(it.toString())}")
                },
            )
        }
        composable(
            route = "${Screens.EditPhotoScreen.route}/{photoUri}",
            arguments = listOf(
                navArgument(name = "photoUri", builder = {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = false
                })
            ),
            deepLinks = listOf()
        ) { backStackEntry ->
            val arg = backStackEntry.arguments?.getString("photoUri", "").orEmpty()
            val decodedUri = Uri.decode(arg)
            val photoUri = decodedUri.toUri()
            ConfirmationSendPhotoScreen(photoUri = photoUri)
        }
    }
}
