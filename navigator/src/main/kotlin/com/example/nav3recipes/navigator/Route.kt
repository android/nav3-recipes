package com.example.nav3recipes.navigator

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    sealed interface Tab : Route {
        @Serializable
        data object Conversations : Tab

        @Serializable
        data object MyProfile : Tab

        @Serializable
        data object Settings : Tab
    }

    @Serializable
    data object Login : Route

    @Serializable
    data object Register : Route

    @Serializable
    data object ForgotPassword : Route

    @Serializable
    data class ConversationDetail(val id: Int) : Route

    @Serializable
    data class ConversationDetailFragment(val id: Int) : Route

    @Serializable
    data object UserProfile : Route
}

