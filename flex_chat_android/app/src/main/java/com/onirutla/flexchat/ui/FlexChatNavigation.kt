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
import com.onirutla.flexchat.conversation.ui.add_new_conversation_screen.AddNewConversationScreenViewModel
import com.onirutla.flexchat.conversation.ui.conversation_screen.ConversationScreen
import com.onirutla.flexchat.core.ui.Screens
import com.onirutla.flexchat.ui.screens.add_new_conversation_screen.AddNewConversationScreen
import com.onirutla.flexchat.ui.screens.add_new_conversation_screen.AddNewConversationScreenUiEvent
import com.onirutla.flexchat.ui.screens.camera_screen.CameraScreen
import com.onirutla.flexchat.ui.screens.confirmation_send_photo_screen.ConfirmationSendPhotoScreen
import com.onirutla.flexchat.ui.screens.confirmation_send_photo_screen.ConfirmationSendPhotoScreenUiEvent
import com.onirutla.flexchat.conversation.ui.confirmation_send_photo_screen.ConfirmationSendPhotoScreenViewModel
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
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screens.MainScreen.route
    ) {
        composable(
            route = Screens.MainScreen.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) {
            val vm: MainScreenViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

//            val notificationPermissionState =
//                rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
//            if (!notificationPermissionState.status.isGranted) {
//                notificationPermissionState.launchPermissionRequest()
//            }

            MainScreen(
                state = state,
                onUiEvent = {
                    when (it) {
                        is MainScreenUiEvent.OnConversationClick -> {
                            navController.navigate(route = "${Screens.Conversation.Default.route}/${it.conversation.id}")
                        }

                        MainScreenUiEvent.OnFloatingActionButtonClick -> {
                            navController.navigate(Screens.AddNewConversationScreen.route)
                        }

                        MainScreenUiEvent.OnProfileIconClick -> {
                            navController.navigate(Screens.ProfileScreen.route)
                        }

                        else -> {}
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
                            navController.navigate(route = "${Screens.Conversation.Default.route}/${it.conversationId}") {
                                popUpTo(route = Screens.AddNewConversationScreen.route) {
                                    inclusive = true
                                }
                            }
                        }

                        else -> {}
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
            route = "${Screens.Conversation.Default.route}/{conversationId}",
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
                        navController.navigate(route = Screens.Conversation.CameraScreen.route)
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

                        ConversationScreenUiEvent.OnAttachmentClick -> {
                            // TODO: Implement add blob to the chat
                        }

                        ConversationScreenUiEvent.OnCameraClick -> {
                            if (!cameraPermissionState.status.isGranted) {
                                cameraPermissionState.launchPermissionRequest()
                            } else {
                                navController.navigate(route = Screens.Conversation.CameraScreen.route)
                            }
                        }

                        ConversationScreenUiEvent.OnEmojiClick -> {
                            // TODO: Implement emoji to the chat
                        }

                        // TODO: Implement voice and or video chat
                        ConversationScreenUiEvent.OnCallClick -> {

                        }

                        ConversationScreenUiEvent.OnVideoCallClick -> {

                        }

                        else -> {}
                    }
                }
            )
        }
        composable(route = Screens.Conversation.CameraScreen.route) {
            CameraScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToEditPhotoScreen = {
                    navController.navigate(
                        route = buildString {
                            append(Screens.Conversation.EditPhotoScreen.route)
                            append("/")
                            append(Uri.encode(it.toString()))
                        }
                    )
                },
            )
        }
        composable(
            route = "${Screens.Conversation.EditPhotoScreen.route}/{photoUri}",
            arguments = listOf(
                navArgument(name = "photoUri", builder = {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = false
                })
            ),
            deepLinks = listOf()
        ) { backStackEntry ->
            val vm: ConfirmationSendPhotoScreenViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            val arg = backStackEntry.arguments?.getString("photoUri", "").orEmpty()
            LaunchedEffect(key1 = arg, block = {
                val photoUri = Uri.decode(arg).toUri()
                vm(photoUri)
            })

            ConfirmationSendPhotoScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = {
                    when (it) {
                        ConfirmationSendPhotoScreenUiEvent.OnClearClick -> {
                            navController.navigateUp()
                        }

                        else -> {}
                    }
                }
            )
        }
    }
}
