package com.example.nav3recipes.modular.hilt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.Serializable

@Serializable
sealed class NavigationState {
    @Serializable
    data object Initializing : NavigationState()

    @Serializable
    data class Anonymous(
        val backStack: List<NavigationEntry> = listOf(Welcome)
    ) : NavigationState()

    @Serializable
    data class Authenticated(
        val currentTab: AuthenticatedTab = ConversationTab,
        val backStack: List<NavigationEntry> = listOf(ConversationTab)
    ) : NavigationState()
}
