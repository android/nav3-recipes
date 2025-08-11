package com.example.nav3recipes.modular.hilt

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable


// Serializable navigation entries
@Serializable
sealed interface NavigationEntry

// Anonymous user routes
@Serializable
data object Welcome : NavigationEntry

@Serializable
data object Login : NavigationEntry

@Serializable
data object Register : NavigationEntry

@Serializable
data object ForgotPassword : NavigationEntry

// Authenticated user routes - main tabs
@Serializable
sealed interface AuthenticatedTab : NavigationEntry {
    val icon: ImageVector
        get() = when (this) {
            is ConversationTab -> Icons.Default.Face
            is MyProfileTab -> Icons.Default.Person
            is SettingsTab -> Icons.Default.Settings
        }
}

@Serializable
data object ConversationTab : AuthenticatedTab

@Serializable
data object MyProfileTab : AuthenticatedTab

@Serializable
data object SettingsTab : AuthenticatedTab

// Conversation sub-routes
@Serializable
data class ConversationDetail(val id: Int) : NavigationEntry

@Serializable
data class ConversationDetailFragment(val id: Int) : NavigationEntry

@Serializable
data object UserProfile : NavigationEntry