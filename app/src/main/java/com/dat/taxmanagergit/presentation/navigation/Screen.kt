package com.dat.taxmanagergit.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
    object Transaction : Screen("transaction_screen")
    object Chart : Screen("chart_screen")
    object Profile : Screen("profile_screen")
}