package com.example.nav3recipes.modular.hilt

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Route : NavKey {
    @Serializable
    data object Profile : Route()

    @Serializable
    data object ConversationList : Route()

    @Serializable
    data class ConversationDetail(val id: Int) : Route()

    @Serializable
    data class ConversationDetailFragment(val id: Int) : Route()
}