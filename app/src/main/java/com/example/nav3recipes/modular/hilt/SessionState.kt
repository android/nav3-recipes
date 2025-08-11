package com.example.nav3recipes.modular.hilt

import android.util.Log
import com.example.nav3recipes.navigator.Route
import kotlinx.serialization.Serializable

@Serializable
sealed class SessionState {
    companion object {
        const val TAG = "SessionState"
    }
    @Serializable
    data object Initializing : SessionState()

    @Serializable
    data class Initialized(val account: Account) : SessionState() {
        val startKey: Route
            get() = when (account.isAuthenticated) {
            true -> Route.ConversationTab
            false -> Route.Login
        }

        fun mutate(topLevelBackStack: TopLevelBackStack<Route>) {
            when (account.isAuthenticated) {
                true -> {
                    when (topLevelBackStack.topLevelKey == Route.Login) {
                        true -> {
                            Log.d(TAG, "User authenticated, switching to ConversationTab")
                            topLevelBackStack.clearAndSet(Route.ConversationTab)
                        }

                        false -> {
                            Log.d(TAG, "Using restored back stack")
                        }
                    }
                }

                false -> {
                    Log.d(TAG, "User logged out, switching to Anonymous")
                    topLevelBackStack.clearAndSet(Route.Login)
                }
            }
        }
    }
}
