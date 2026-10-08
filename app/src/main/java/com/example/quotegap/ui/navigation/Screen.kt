package com.example.quotegap.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Calculator : Screen("calculator")
    object History : Screen("history")
    object Settings : Screen("settings")
    object About : Screen("about")
}
