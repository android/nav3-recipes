package com.example.nav3recipes.modular.hilt

import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * NavigationViewModel manages the navigation state for the modular navigation system.
 *
 * The navigation is managed through a combination of:
 * - Android Navigation 3 runtime for back stack persistence and restoration
 * - Session management for authentication state via reactive Flow
 * - Tab-based navigation for authenticated users
 *
 * ## Navigation Architecture:
 *
 * ### Initialization Flow:
 * - Always starts with Welcome screen to check session state
 * - Combines Session.account flow with backStack to determine navigation state
 * - If account is authenticated and restored stack contains authenticated entries: restore authenticated state
 * - If account is authenticated but no authenticated entries: start fresh authenticated state
 * - If account is anonymous: show anonymous flow starting with Welcome
 *
 * ### Anonymous State:
 * - Single back stack for unauthenticated users
 * - Contains welcome, login, register, and forgot password screens
 *
 * ### Authenticated State:
 * - Tab-based navigation with separate stacks per tab
 * - Conversation Tab: List and detail screens
 * - Profile Tab: User profile management
 * - Settings Tab: App configuration and logout
 *
 * ## Session Management:
 * - Reactively observes [Session.account] Flow for authentication changes
 * - Restores authenticated state on app restart if session is still valid
 * - Explicit login/logout only - no automatic session refresh or activity tracking
 *
 * ## Back Stack Restoration:
 * - Leverages `rememberNavBackStack` for automatic serialization
 * - On restoration, validates current account to determine if authenticated stack should be used
 * - Falls back to anonymous state if session expired
 *
 * @param backStack The back stack managed by Navigation 3 runtime
 * @param session The session manager for authentication state
 */
@HiltViewModel(assistedFactory = NavigationViewModel.Factory::class)
class NavigationViewModel @AssistedInject constructor(
    @Assisted private val backStack: SnapshotStateList<NavKey>,
    private val session: Session
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(backStack: SnapshotStateList<NavKey>): NavigationViewModel
    }

    companion object {
        private const val TAG = "NavigationViewModel"
    }

    private val _backStackState = MutableStateFlow(backStack.toList())
    private var currentTab: AuthenticatedTab = ConversationTab

    // Process account changes and update backstack accordingly
    private val processedAccountFlow = session.account
        .onEach { account ->
            val hasAuthenticatedEntries = backStack.any {
                it is AuthenticatedTab || isAuthenticatedEntry(it)
            }

            when {
                account.isAuthenticated && !hasAuthenticatedEntries -> {
                    Log.d(TAG, "User authenticated but no authenticated entries, starting fresh")
                    // Start fresh authenticated state
                    updateBackStack {
                        clear()
                        add(ConversationTab)
                        currentTab = ConversationTab
                    }
                }

                !account.isAuthenticated && hasAuthenticatedEntries -> {
                    Log.d(TAG, "User not authenticated but has authenticated entries, clearing")
                    // Session expired or user logged out, clear authenticated entries
                    updateBackStack {
                        clear()
                        add(Welcome)
                    }
                }
            }
        }

    // Combine processed account flow with backstack to determine navigation state
    val navigationState: StateFlow<NavigationState> = combine(
        processedAccountFlow,
        _backStackState.asStateFlow()
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

    private inline fun updateBackStack(action: SnapshotStateList<NavKey>.() -> Unit) {
        backStack.action()
        _backStackState.value = backStack.toList()
    }

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

            // Clear current stack and start with conversation tab
            updateBackStack {
                clear()
                add(ConversationTab)
                currentTab = ConversationTab
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "User logging out")
            session.logout()

            // Clear stack and return to welcome
            updateBackStack {
                clear()
                add(Welcome)
            }
        }
    }

    fun navigateToTab(tab: AuthenticatedTab) {
        viewModelScope.launch {
            Log.d(TAG, "Switching to tab: $tab")

            // Update current tab and replace current stack with just the tab root
            updateBackStack {
                clear()
                add(tab)
                currentTab = tab
            }
        }
    }

    fun navigateToEntry(entry: NavigationEntry) {
        viewModelScope.launch {
            Log.d(TAG, "Adding $entry to stack")
            // Add to back stack
            updateBackStack {
                add(entry)
            }
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.size > 1) {
            viewModelScope.launch {
                val removedEntry = backStack.removeLastOrNull()
                Log.d(TAG, "Removed $removedEntry from back stack")

                // Update current tab if we're back to a tab
                val lastTab = backStack.reversed()
                    .firstOrNull { it is AuthenticatedTab } as? AuthenticatedTab
                if (lastTab != null) {
                    currentTab = lastTab
                }

                _backStackState.value = backStack.toList()
            }
            return true
        }
        return false
    }
}
