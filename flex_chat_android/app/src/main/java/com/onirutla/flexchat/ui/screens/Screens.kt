package com.onirutla.flexchat.ui.screens

sealed class Screens(val route: String) {
    data object MainScreen : Screens("/")
    data object LoginScreen : Screens("/users/login")
    data object RegisterScreen : Screens("/users/register")
}
