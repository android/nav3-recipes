package com.example.nav3recipes.modular.hilt

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
fun rememberNavigator(startDestination: Route): Navigator {
    val backStack = rememberNavBackStack(startDestination)
    return Navigator(backStack)
}

class Navigator(val backStack: NavBackStack) {
    fun goTo(destination: Route) {
        backStack.add(destination)
    }

    fun goBack() {
        backStack.removeLastOrNull()
    }
}