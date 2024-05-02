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

package com.onirutla.flexchat.conversation.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.onirutla.flexchat.conversation.ui.add_new_conversation.AddNewConversationScreen
import com.onirutla.flexchat.conversation.ui.add_new_conversation.AddNewConversationUiEvent
import com.onirutla.flexchat.conversation.ui.add_new_conversation.AddNewConversationViewModel
import com.onirutla.flexchat.conversation.ui.call.CallActivity
import com.onirutla.flexchat.conversation.ui.camera.CameraScreen
import com.onirutla.flexchat.conversation.ui.confirmation_send_photo.ConfirmationSendPhotoScreen
import com.onirutla.flexchat.conversation.ui.confirmation_send_photo.ConfirmationSendPhotoUiEvent
import com.onirutla.flexchat.conversation.ui.confirmation_send_photo.ConfirmationSendPhotoViewModel
import com.onirutla.flexchat.conversation.ui.conversation_room.ConversationRoomScreen
import com.onirutla.flexchat.conversation.ui.conversation_room.ConversationRoomUiEvent
import com.onirutla.flexchat.conversation.ui.conversation_room.ConversationRoomViewModel
import com.onirutla.flexchat.conversation.ui.conversations.ConversationsScreen
import com.onirutla.flexchat.conversation.ui.conversations.ConversationsUiEvent
import com.onirutla.flexchat.conversation.ui.conversations.ConversationsViewModel
import com.onirutla.flexchat.core.ui.Screens
import com.onirutla.flexchat.core.webrtc.FlexChatPeerConnectionFactory
import com.onirutla.flexchat.core.webrtc.SignalingClient
import com.onirutla.flexchat.core.webrtc.WebRtcSessionManager
import com.onirutla.flexchat.core.webrtc.WebRtcSessionManagerImpl
import com.onirutla.flexchat.user.ui.profile_screen.ProfileScreenViewModel
import com.onirutla.flexchat.user.ui.profile_screen.ProfileScreen
import com.onirutla.flexchat.user.ui.profile_screen.ProfileScreenUiEvent
import timber.log.Timber

fun NavGraphBuilder.conversationNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Screens.Conversation.ConversationList.route,
        route = Screens.Conversation.Default.route
    ) {
        composable(
            route = Screens.Conversation.ConversationList.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) {
            val vm: ConversationsViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            ConversationsScreen(
                state = state,
                onUiEvent = {
                    when (it) {
                        is ConversationsUiEvent.OnConversationClick -> {
                            navController.navigate(route = "${Screens.Conversation.Default.route}/${it.conversation.id}")
                        }

                        ConversationsUiEvent.OnFloatingActionButtonClick -> {
                            navController.navigate(Screens.Conversation.AddNewConversationScreen.route)
                        }

                        ConversationsUiEvent.OnProfileIconClick -> {
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
            val vm: AddNewConversationViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            AddNewConversationScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = {
                    when (it) {
                        AddNewConversationUiEvent.OnNavigateUpClick -> {
                            navController.navigateUp()
                        }

                        is AddNewConversationUiEvent.OnNavigateToConversationScreen -> {
                            navController.navigate(route = "${Screens.Conversation.Default.route}/${it.value}") {
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
            val vm: ConversationRoomViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            val conversationId = backStackEntry.arguments?.getString("conversationId", "").orEmpty()
            LaunchedEffect(key1 = conversationId) {
                vm(conversationId)
            }

            val context = LocalContext.current

            ConversationRoomScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = { event ->
                    when (event) {
                        ConversationRoomUiEvent.OnNavigateUp -> {
                            navController.navigateUp()
                        }

                        ConversationRoomUiEvent.OnAttachmentClick -> {
                            // TODO: Implement add blob to the chat
                        }

                        ConversationRoomUiEvent.OnCameraClick -> {
//                            if (!cameraPermissionState.status.isGranted) {
//                                cameraPermissionState.launchPermissionRequest()
//                            } else {
//                                navController.navigate(route = Screens.Conversation.CameraScreen.route)
//                            }
                        }

                        ConversationRoomUiEvent.OnEmojiClick -> {
                            // TODO: Implement emoji to the chat
                        }

                        // TODO: Implement voice and or video chat
                        ConversationRoomUiEvent.OnCallClick -> {
                            Intent(context, CallActivity::class.java).apply {
                                putExtra("conversationId", state.conversation.id)
                                putExtra("callInitiatorId", state.currentUser.id)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }.also {
                                context.startActivity(it)
                            }
                        }

                        ConversationRoomUiEvent.OnVideoCallClick -> {
//
                        }

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
            val vm: ConfirmationSendPhotoViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            val arg = backStackEntry.arguments?.getString("photoUri", "").orEmpty()
            LaunchedEffect(key1 = arg) {
                val photoUri = Uri.decode(arg).toUri()
                vm(photoUri)
            }

            ConfirmationSendPhotoScreen(
                state = state,
                onEvent = vm::onEvent,
                onUiEvent = {
                    when (it) {
                        ConfirmationSendPhotoUiEvent.OnClearClick -> {
                            navController.navigateUp()
                        }
                    }
                }
            )
        }

//        route = "${Screens.Conversation.Default.route}/{conversationId}",
//        arguments = listOf(
//            navArgument(name = "conversationId") {
//                type = NavType.StringType
//                defaultValue = ""
//                nullable = false
//            }
//        ),
//        deepLinks = listOf(),

        composable(
            route = "${Screens.Conversation.CallScreen.route}/{conversationId}",
            arguments = listOf(
                navArgument(name = "conversationId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = false
                }
            ),
            deepLinks = listOf()
        ) {
            val context = LocalContext.current
            val vm: ConversationRoomViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            val conversationId = it.arguments?.getString("conversationId", "").orEmpty()
            val callSession: WebRtcSessionManager = rememberSaveable {
                Timber.d(state.currentUser.id)
                WebRtcSessionManagerImpl(
                    context = context,
                    signalingClient = SignalingClient(vm.firestore),
                    peerConnectionFactory = FlexChatPeerConnectionFactory(context),
                    conversationId = conversationId,
                    callInitiatorId = state.currentUser.id
                )
            }

            val callSessionState = callSession.signalingClient
                .sessionStateFlow
                .collectAsStateWithLifecycle()


        }

    }
}
