package com.example.nav3recipes.modular.hilt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.conversation.ConversationDetailScreen
import com.example.nav3recipes.conversation.ConversationId
import com.example.nav3recipes.conversation.ConversationListScreen
import com.example.nav3recipes.profile.ProfileScreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import dagger.hilt.android.AndroidEntryPoint

/**
 * This recipe demonstrates how to use a modular approach with Navigation 3,
 * where different parts of the application are defined in separate modules and injected
 * into the main app using Dagger/Hilt.
 *
 * Features (Conversation and Profile) are split into two modules:
 * - api: defines the public facing routes for this feature
 * - impl: defines the entryProviders for this feature, these are injected into the app's main activity
 * The common module defines:
 * - a common navigator class that exposes a back stack and methods to modify that back stack
 * - a type that should be used by feature modules to inject entryProviders into the app's main activity
 * The app module creates the navigator by supplying a start destination and provides this navigator
 * to the rest of the app module (i.e. MainActivity) and the feature modules.
 */
@AndroidEntryPoint
class ModularActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEdgeToEdgeConfig()
        val navigator = Navigator(startDestination = Route.ConversationList)
        setContent {
            NavDisplay(
                backStack = navigator.backStack,
                onBack = { navigator.goBack() },
                entryProvider = entryProvider {
                    entry<Route.ConversationList> {
                        ConversationListScreen(
                            onConversationClicked = { conversationId ->
                                navigator.goTo(Route.ConversationDetail(conversationId.value))
                            }
                        )
                    }
                    entry<Route.ConversationDetail> { key ->
                        ConversationDetailScreen(
                            conversationId = ConversationId(key.id),
                            onProfileClicked = {
                                navigator.goTo(Route.Profile)
                            }
                        )
                    }
                    entry<Route.Profile> {
                        ProfileScreen()
                    }
                }
            )
        }
    }
}
