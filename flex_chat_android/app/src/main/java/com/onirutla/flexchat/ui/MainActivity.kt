package com.onirutla.flexchat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.onirutla.flexchat.ui.screens.Screens
import com.onirutla.flexchat.ui.screens.main_screen.MainScreen
import com.onirutla.flexchat.ui.screens.main_screen.MainScreenState
import com.onirutla.flexchat.ui.theme.FlexChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlexChatTheme {
                FlexChatApp(modifier = Modifier, navController = rememberNavController())
            }
        }
    }
}

@Composable
fun FlexChatApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    FlexChatTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            FlexChatNavigation(modifier = modifier, navController = navController)
        }
    }
}

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
            MainScreen(
                state = MainScreenState(),
                onUiEvent = {}
            )
        }
        composable(
            route = Screens.LoginScreen.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) {

        }
        composable(
            route = Screens.RegisterScreen.route,
            arguments = listOf(),
            deepLinks = listOf()
        ) {

        }
    }
}
