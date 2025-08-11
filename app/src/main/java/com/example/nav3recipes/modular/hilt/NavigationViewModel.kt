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
import kotlinx.coroutines.launch

/**
 * NavigationViewModel manages the navigation state for the modular navigation system.
 *
 * The navigation is managed through a combination of:
 * - Android Navigation 3 runtime for back stack persistence and restoration
 * - Session management for authentication state
 * - Tab-based navigation for authenticated users
 *
 * ## Navigation Architecture:
 *
 * ### Initialization Flow:
 * - Always starts with Welcome screen to check session state
 * - If session is active and restored stack contains authenticated entries: restore authenticated state
 * - If session is active but no authenticated entries: start fresh authenticated state
 * - If session is inactive: show anonymous flow starting with Welcome
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
 * - Uses [Session] to manage authentication state with 2-minute timeout
 * - Automatically validates session on navigation operations
 * - Logs out user when session expires
 * - Restores authenticated state on app restart if session is still valid
 *
 * ## Back Stack Restoration:
 * - Leverages `rememberNavBackStack` for automatic serialization
 * - On restoration, validates session to determine if authenticated stack should be used
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
        private val AUTHENTICATED_TABS: List<AuthenticatedTab> = listOf(
            ConversationTab, MyProfileTab, SettingsTab
        )
    }

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Initializing)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    private var currentTab: AuthenticatedTab = ConversationTab

    init {
        // Handle initialization based on session state and restored back stack
        viewModelScope.launch {
            initializeNavigationState()
        }
    }

    private suspend fun initializeNavigationState() {
        val isSessionActive = session.isSessionActive()
        val hasAuthenticatedEntries =
            backStack.any { it is AuthenticatedTab || isAuthenticatedEntry(it) }

        when {
            isSessionActive && hasAuthenticatedEntries -> {
                Log.d(TAG, "Restoring authenticated session with saved back stack")
                // Determine current tab from the back stack
                currentTab =
                    backStack.reversed().firstOrNull { it is AuthenticatedTab } as? AuthenticatedTab
                        ?: ConversationTab
                _navigationState.value = NavigationState.Authenticated(
                    currentTab = currentTab,
                    backStack = backStack.mapNotNull { it as? NavigationEntry }
                )
            }

            isSessionActive -> {
                Log.d(
                    TAG,
                    "Session active but no authenticated entries, starting fresh authenticated state"
                )
                // Replace Welcome with first tab
                backStack.clear()
                backStack.add(ConversationTab)
                currentTab = ConversationTab
                _navigationState.value = NavigationState.Authenticated(
                    currentTab = ConversationTab,
                    backStack = listOf(ConversationTab)
                )
            }

            hasAuthenticatedEntries -> {
                Log.d(TAG, "Session expired, discarding authenticated back stack")
                backStack.clear()
                backStack.add(Welcome)
                _navigationState.value = NavigationState.Anonymous()
            }

            else -> {
                Log.d(TAG, "Starting with anonymous state")
                _navigationState.value = NavigationState.Anonymous()
            }
        }
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
            Log.d(TAG, "User authenticated")
            session.startSession()

            // Clear current stack and start with conversation tab
            backStack.clear()
            backStack.add(ConversationTab)
            currentTab = ConversationTab

            _navigationState.value = NavigationState.Authenticated(
                currentTab = ConversationTab,
                backStack = listOf(ConversationTab)
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "User logged out")
            session.endSession()

            // Clear stack and return to welcome
            backStack.clear()
            backStack.add(Welcome)

            _navigationState.value = NavigationState.Anonymous()
        }
    }

    fun navigateToTab(tab: AuthenticatedTab) {
        viewModelScope.launch {
            if (!session.checkAndUpdateSession()) {
                Log.d(TAG, "Session expired when switching tabs, logging out")
                logout()
                return@launch
            }

            val currentState = _navigationState.value
            if (currentState is NavigationState.Authenticated) {
                Log.d(TAG, "Switching to tab: $tab")

                // Update current tab
                currentTab = tab

                // Replace current stack with just the tab root
                backStack.clear()
                backStack.add(tab)

                _navigationState.value = currentState.copy(
                    currentTab = tab,
                    backStack = listOf(tab)
                )
            }
        }
    }

    fun navigateToEntry(entry: NavigationEntry) {
        viewModelScope.launch {
            val currentState = _navigationState.value

            when (currentState) {
                is NavigationState.Authenticated -> {
                    if (!session.checkAndUpdateSession()) {
                        Log.d(TAG, "Session expired when navigating, logging out")
                        logout()
                        return@launch
                    }

                    // Add to back stack
                    backStack.add(entry)
                    Log.d(TAG, "Adding $entry to authenticated stack")
                    _navigationState.value = currentState.copy(
                        backStack = backStack.mapNotNull { it as? NavigationEntry }
                    )
                }

                is NavigationState.Anonymous -> {
                    // Add to back stack
                    backStack.add(entry)
                    Log.d(TAG, "Adding $entry to anonymous stack")
                    _navigationState.value = currentState.copy(
                        backStack = backStack.mapNotNull { it as? NavigationEntry }
                    )
                }

                is NavigationState.Initializing -> {
                    Log.d(TAG, "Navigation attempted during initialization, ignoring")
                }
            }
        }
    }

    fun navigateBack(): Boolean {
        val currentState = _navigationState.value

        return when (currentState) {
            is NavigationState.Authenticated -> {
                // Check session
                viewModelScope.launch {
                    if (!session.checkAndUpdateSession()) {
                        Log.d(TAG, "Session expired on back navigation, logging out")
                        logout()
                        return@launch
                    }
                }

                if (backStack.size > 1) {
                    val removedEntry = backStack.removeLastOrNull()
                    Log.d(TAG, "Removed $removedEntry from back stack")

                    // Update current tab if we're back to a tab
                    val lastTab = backStack.reversed()
                        .firstOrNull { it is AuthenticatedTab } as? AuthenticatedTab
                    if (lastTab != null) {
                        currentTab = lastTab
                    }

                    _navigationState.value = currentState.copy(
                        currentTab = currentTab,
                        backStack = backStack.mapNotNull { it as? NavigationEntry }
                    )
                    true
                } else {
                    false
                }
            }

            is NavigationState.Anonymous -> {
                if (backStack.size > 1) {
                    val removedEntry = backStack.removeLastOrNull()
                    Log.d(TAG, "Removed $removedEntry from anonymous stack")
                    _navigationState.value = currentState.copy(
                        backStack = backStack.mapNotNull { it as? NavigationEntry }
                    )
                    true
                } else {
                    false
                }
            }

            is NavigationState.Initializing -> {
                false
            }
        }
    }
}
