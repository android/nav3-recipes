package com.example.nav3recipes.modular.hilt

import kotlinx.serialization.Serializable
import com.example.nav3recipes.modular.hilt.Anonymous as AnonymousTab

@Serializable
sealed class NavigationState {
    @Serializable
    data object Initializing : NavigationState()

    @Serializable
    data class Anonymous(
        val backStack: List<NavigationEntry> = listOf(AnonymousTab)
    ) : NavigationState()

    @Serializable
    data class Authenticated(
        val currentTab: AuthenticatedTab = ConversationTab,
        val backStack: List<NavigationEntry> = listOf(ConversationTab)
    ) : NavigationState()
}
