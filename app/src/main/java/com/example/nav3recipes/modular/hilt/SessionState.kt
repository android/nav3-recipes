package com.example.nav3recipes.modular.hilt

import android.util.Log
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
        val startKey: NavigationEntry get() = when (account.isAuthenticated) {
            true -> ConversationTab
            false -> Login
        }

        fun mutate(topLevelBackStack: TopLevelBackStack<NavigationEntry>) {
            when (account.isAuthenticated) {
                true -> {
                    when (topLevelBackStack.topLevelKey == Login) {
                        true -> {
                            Log.d(TAG, "User authenticated, switching to ConversationTab")
                            topLevelBackStack.clearAndSet(ConversationTab)
                        }

                        false -> {
                            Log.d(TAG, "Using restored back stack")
                        }
                    }
                }

                false -> {
                    Log.d(TAG, "User logged out, switching to Anonymous")
                    topLevelBackStack.clearAndSet(Login)
                }
            }
        }
    }
}
