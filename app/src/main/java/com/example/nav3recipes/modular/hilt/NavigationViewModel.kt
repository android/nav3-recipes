package com.example.nav3recipes.modular.hilt

import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * NavigationViewModel manages the navigation state for the modular navigation system.
 *
 * The navigation is managed through a combination of:
 * - TopLevelBackStack for managing separate stacks per tab/section
 * - Session management for authentication state via reactive Flow
 * - Navigation 3 runtime for automatic serialization and restoration
 *
 * ## Navigation Architecture:
 *
 * ### Initialization Flow:
 * - Always starts with Welcome screen to check session state
 * - Combines Session.account flow with TopLevelBackStack state to determine navigation state
 * - Uses TopLevelBackStack to maintain separate stacks for different sections (anonymous vs authenticated tabs)
 *
 * ### Anonymous State:
 * - Single stack managed by TopLevelBackStack with Welcome as the top-level key
 * - Contains welcome, login, register, and forgot password screens
 *
 * ### Authenticated State:
 * - Multiple stacks managed by TopLevelBackStack, one per authenticated tab
 * - Conversation Tab: List and detail screens
 * - Profile Tab: User profile management
 * - Settings Tab: App configuration and logout
 *
 * ## Session Management:
 * - Reactively observes [Session.account] Flow for authentication changes
 * - Automatically manages TopLevelBackStack based on authentication state
 * - Explicit login/logout only - no automatic session refresh or activity tracking
 *
 * ## Back Stack Management:
 * - Uses TopLevelBackStack which internally uses Navigation 3's rememberNavBackStack
 * - All modifications are automatically serialized by Navigation 3 runtime
 * - State changes trigger UI updates and proper persistence
 *
 * @param topLevelBackStack The TopLevelBackStack managing navigation state
 * @param session The session manager for authentication state
 */
@HiltViewModel(assistedFactory = NavigationViewModel.Factory::class)
class NavigationViewModel @AssistedInject constructor(
    @Assisted private val topLevelBackStack: TopLevelBackStack<NavigationEntry>,
    private val session: Session
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(topLevelBackStack: TopLevelBackStack<NavigationEntry>): NavigationViewModel
    }

    companion object {
        private const val TAG = "NavigationViewModel"
    }

    private val backStackStateFlow = snapshotFlow { topLevelBackStack.backStack.toList() }
    private var currentTab: AuthenticatedTab = ConversationTab

    // Process account changes and update TopLevelBackStack accordingly
    private val processedAccountFlow = session.account
        .onEach { account ->
            val hasAuthenticatedEntries = topLevelBackStack.backStack.any {
                it is AuthenticatedTab || isAuthenticatedEntry(it)
            }

            when {
                account.isAuthenticated && !hasAuthenticatedEntries -> {
                    Log.d(TAG, "User authenticated but no authenticated entries, starting fresh")
                    // Start fresh authenticated state with conversation tab
                    topLevelBackStack.clearAndSet(ConversationTab)
                    currentTab = ConversationTab
                }

                !account.isAuthenticated && hasAuthenticatedEntries -> {
                    Log.d(TAG, "User not authenticated but has authenticated entries, clearing")
                    // Session expired or user logged out, clear authenticated entries
                    topLevelBackStack.clearAndSet(Welcome)
                }
            }
        }

    // Combine processed account flow with TopLevelBackStack state to determine navigation state  
    val navigationState: StateFlow<NavigationState> = combine(
        processedAccountFlow,
        backStackStateFlow
    ) { account, backStackEntries ->
        when {
            account.isAuthenticated -> {
                val hasAuthenticatedEntries = backStackEntries.any {
                    it is AuthenticatedTab || isAuthenticatedEntry(it)
                }

                if (hasAuthenticatedEntries) {
                    // Determine current tab from the back stack
                    currentTab = backStackEntries.reversed()
                        .firstOrNull { it is AuthenticatedTab } as? AuthenticatedTab
                        ?: ConversationTab

                    NavigationState.Authenticated(
                        currentTab = currentTab,
                        backStack = backStackEntries.mapNotNull { it as? NavigationEntry }
                    )
                } else {
                    // Authenticated but no authenticated entries - start fresh
                    NavigationState.Authenticated(
                        currentTab = ConversationTab,
                        backStack = listOf(ConversationTab)
                    )
                }
            }
            else -> {
                NavigationState.Anonymous(
                    backStack = backStackEntries.mapNotNull { it as? NavigationEntry }
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
        initialValue = NavigationState.Initializing
    )


    private fun isAuthenticatedEntry(entry: NavKey): Boolean {
        return when (entry) {
            is AuthenticatedTab -> true
            is ConversationDetail, is ConversationDetailFragment, is UserProfile -> true
            else -> false
        }
    }

    fun authenticate() {
        viewModelScope.launch {
            Log.d(TAG, "User authenticating")
            session.login()

            // Start fresh authenticated state with conversation tab
            topLevelBackStack.clearAndSet(ConversationTab)
            currentTab = ConversationTab
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "User logging out")
            session.logout()

            // Clear all stacks and return to welcome
            topLevelBackStack.clearAndSet(Welcome)
        }
    }

    fun navigateToTab(tab: AuthenticatedTab) {
        viewModelScope.launch {
            Log.d(TAG, "Switching to tab: $tab")

            // Switch to the specified tab (creates new stack if needed)
            topLevelBackStack.addTopLevel(tab)
            currentTab = tab
        }
    }

    fun navigateToEntry(entry: NavigationEntry) {
        viewModelScope.launch {
            Log.d(TAG, "Adding $entry to current stack")

            // Add to current stack
            topLevelBackStack.add(entry)
        }
    }

    fun navigateBack(): Boolean {
        return if (topLevelBackStack.backStack.size > 1) {
            Log.d(TAG, "Navigating back")

            topLevelBackStack.removeLast()

            // Update current tab if we're back to a tab
            val lastTab = topLevelBackStack.backStack.reversed()
                .firstOrNull { it is AuthenticatedTab } as? AuthenticatedTab
            if (lastTab != null) {
                currentTab = lastTab
            }

            true
        } else {
            false
        }
    }
}
