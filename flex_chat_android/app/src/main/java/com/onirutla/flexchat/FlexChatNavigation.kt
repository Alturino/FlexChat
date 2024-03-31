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

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.onirutla.flexchat.auth.ui.AuthViewModel
import com.onirutla.flexchat.auth.ui.authNavGraph
import com.onirutla.flexchat.conversation.ui.conversationNavGraph
import com.onirutla.flexchat.core.ui.Screens

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FlexChatNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val authVm: AuthViewModel = hiltViewModel()
    val isLoggedIn by authVm.isLoggedIn.collectAsStateWithLifecycle(null)
    val currentDestination by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
        initialValue = null
    )

    LaunchedEffect(key1 = isLoggedIn) {
        if (isLoggedIn == true) {
            navController.navigate(route = Screens.Conversation.Default.route) {
                popUpTo(route = Screens.Auth.Default.route) {
                    saveState = true
                    inclusive = true
                }
            }
        }
        if (isLoggedIn == false) {
            navController.navigate(route = Screens.Auth.Default.route) {
                popUpTo(route = Screens.Conversation.Default.route) {
                    saveState = true
                    inclusive = true
                }
            }
        }
    }

    val notificationPermissionState = rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS
    )
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = {
            if (it) {
                navController.navigate(route = Screens.Conversation.CameraScreen.route)
            }
        }
    )
    LaunchedEffect(key1 = currentDestination) {
        if (currentDestination?.destination?.route == Screens.Conversation.Default.route) {
            if (!notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
        if (currentDestination?.destination?.route == Screens.Conversation.CameraScreen.route) {
            if (!cameraPermissionState.status.isGranted) {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screens.Conversation.Default.route
    ) {
        authNavGraph(navController)
        conversationNavGraph(navController)
    }
}
