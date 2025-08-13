package com.example.nav3recipes.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

interface NavBackStack {
    fun add(route: Route)
    fun pop()
    val size: Int
}

val LocalNavBackStack =
    compositionLocalOf<NavBackStack> { error("No NavBackStack provided!") }

@Composable
fun ProvideNavBackStack(instance: NavBackStack, content: @Composable () -> Unit) {
    CompositionLocalProvider(value = LocalNavBackStack provides instance, content = content)
}
