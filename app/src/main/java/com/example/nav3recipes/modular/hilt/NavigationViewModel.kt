package com.example.nav3recipes.modular.hilt

import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * @param restoredBackStack The back stack restored by Navigation 3 runtime
 * @param session The session manager for authentication state
 */
@HiltViewModel(assistedFactory = NavigationViewModel.Factory::class)
class NavigationViewModel @AssistedInject constructor(
    @Assisted private val restoredBackStack: SnapshotStateList<NavigationEntry>,
    private val session: Session
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(restoredBackStack: SnapshotStateList<NavigationEntry>): NavigationViewModel
    }

    companion object {
        private const val TAG = "NavigationViewModel"
        private val AUTHENTICATED_TABS: List<AuthenticatedTab> = listOf(
            ConversationTab, MyProfileTab, SettingsTab
        )
    }

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Anonymous())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    private val tabStacks = mutableMapOf<AuthenticatedTab, MutableList<NavigationEntry>>()

    init {
        // Initialize tab stacks
        AUTHENTICATED_TABS.forEach { tab ->
            tabStacks[tab] = mutableListOf(tab)
        }

        // Handle restored back stack based on session state
        viewModelScope.launch {
            initializeNavigationState()
        }
    }

    private suspend fun initializeNavigationState() {
        val isSessionActive = session.isSessionActive()
        val hasAuthenticatedEntries =
            restoredBackStack.any { it is AuthenticatedTab || isAuthenticatedEntry(it) }

        when {
            isSessionActive && hasAuthenticatedEntries -> {
                Log.d(TAG, "Restoring authenticated session with saved back stack")
                restoreAuthenticatedState()
            }

            isSessionActive -> {
                Log.d(
                    TAG,
                    "Session active but no authenticated entries, starting fresh authenticated state"
                )
                _navigationState.value = NavigationState.Authenticated(
                    currentTab = ConversationTab,
                    backStack = tabStacks[ConversationTab]?.toList() ?: listOf(ConversationTab)
                )
            }
            hasAuthenticatedEntries -> {
                Log.d(TAG, "Session expired, discarding authenticated back stack")
                restoredBackStack.clear()
                restoredBackStack.add(Welcome)
                _navigationState.value = NavigationState.Anonymous()
            }

            else -> {
                Log.d(TAG, "Starting with anonymous state")
                _navigationState.value = NavigationState.Anonymous()
            }
        }
    }

    private fun isAuthenticatedEntry(entry: NavigationEntry): Boolean {
        return when (entry) {
            is AuthenticatedTab -> true
            is ConversationDetail, is ConversationDetailFragment, is UserProfile -> true
            else -> false
        }
    }

    private fun restoreAuthenticatedState() {
        // Rebuild tab stacks from restored back stack
        val currentTab =
            restoredBackStack.lastOrNull { it is AuthenticatedTab } as? AuthenticatedTab
                ?: ConversationTab

        // Group entries by tab
        var currentTabForEntry: AuthenticatedTab = ConversationTab
        tabStacks.clear()
        AUTHENTICATED_TABS.forEach { tab -> tabStacks[tab] = mutableListOf() }

        for (entry in restoredBackStack) {
            when (entry) {
                is AuthenticatedTab -> {
                    currentTabForEntry = entry
                    tabStacks[currentTabForEntry]?.add(entry)
                }

                is ConversationDetail, is ConversationDetailFragment -> {
                    // These belong to conversation tab
                    if (tabStacks[ConversationTab]?.isEmpty() == true) {
                        tabStacks[ConversationTab]?.add(ConversationTab)
                    }
                    tabStacks[ConversationTab]?.add(entry)
                }

                is UserProfile -> {
                    // This can belong to any tab, add to current
                    tabStacks[currentTabForEntry]?.add(entry)
                }

                // Anonymous entries should not be in authenticated state
                is Welcome, is Login, is Register, is ForgotPassword -> {
                    // Skip these entries when restoring authenticated state
                }
            }
        }

        // Ensure each tab has at least its root
        AUTHENTICATED_TABS.forEach { tab ->
            if (tabStacks[tab]?.isEmpty() == true) {
                tabStacks[tab]?.add(tab)
            }
        }

        _navigationState.value = NavigationState.Authenticated(
            currentTab = currentTab,
            backStack = restoredBackStack.toList()
        )
    }

    fun authenticate() {
        viewModelScope.launch {
            Log.d(TAG, "User authenticated")
            session.startSession()

            // Clear anonymous entries and start with conversation tab
            restoredBackStack.clear()
            restoredBackStack.add(ConversationTab)

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

            // Reset tab stacks and back stack
            AUTHENTICATED_TABS.forEach { tab ->
                tabStacks[tab] = mutableListOf(tab)
            }

            restoredBackStack.clear()
            restoredBackStack.add(Welcome)

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

                // Update back stack to show this tab's stack
                val tabStack = tabStacks[tab] ?: listOf(tab)
                restoredBackStack.clear()
                restoredBackStack.addAll(tabStack)

                _navigationState.value = currentState.copy(
                    currentTab = tab,
                    backStack = tabStack
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

                    // Add to current tab's stack and main back stack
                    val currentTabStack: MutableList<NavigationEntry> =
                        tabStacks[currentState.currentTab]?.toMutableList() ?: mutableListOf(
                            currentState.currentTab
                        )
                    currentTabStack.add(entry)
                    tabStacks[currentState.currentTab] = currentTabStack

                    restoredBackStack.add(entry)

                    Log.d(TAG, "Adding $entry to tab ${currentState.currentTab}")
                    _navigationState.value =
                        currentState.copy(backStack = restoredBackStack.toList())
                }

                is NavigationState.Anonymous -> {
                    // Add to back stack
                    restoredBackStack.add(entry)
                    Log.d(TAG, "Adding $entry to anonymous stack")
                    _navigationState.value =
                        currentState.copy(backStack = restoredBackStack.toList())
                }
            }
        }
    }

    fun navigateBack(): Boolean {
        val currentState = _navigationState.value

        return when (currentState) {
            is NavigationState.Authenticated -> {
                // Check session in a coroutine but return immediately based on current stack
                viewModelScope.launch {
                    if (!session.checkAndUpdateSession()) {
                        Log.d(TAG, "Session expired on back navigation, logging out")
                        logout()
                        return@launch
                    }
                }

                if (restoredBackStack.size > 1) {
                    val removedEntry = restoredBackStack.removeLastOrNull()

                    // Also remove from tab stack if it's there
                    val currentTabStack = tabStacks[currentState.currentTab]
                    if (currentTabStack != null && currentTabStack.lastOrNull() == removedEntry && currentTabStack.size > 1) {
                        currentTabStack.removeLastOrNull()
                    }

                    Log.d(TAG, "Removed $removedEntry from back stack")
                    _navigationState.value =
                        currentState.copy(backStack = restoredBackStack.toList())
                    true
                } else {
                    false
                }
            }

            is NavigationState.Anonymous -> {
                if (restoredBackStack.size > 1) {
                    val removedEntry = restoredBackStack.removeLastOrNull()
                    Log.d(TAG, "Removed $removedEntry from anonymous stack")
                    _navigationState.value =
                        currentState.copy(backStack = restoredBackStack.toList())
                    true
                } else {
                    false
                }
            }
        }
    }
}
