package com.example.nav3recipes.modular.hilt

import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * The navigation is managed through:
 * - TopLevelBackStack for managing separate stacks per tab/section
 * - Session management for authentication state via reactive Flow
 * - Direct manipulation of TopLevelBackStack based on session state
 *
 * ## Navigation Architecture:
 *
 * ### Anonymous State:
 * - Uses Anonymous as the top-level key with Login as default initial route
 * - Single stack managed by TopLevelBackStack
 *
 * ### Authenticated State:
 * - Multiple stacks managed by TopLevelBackStack, one per authenticated tab
 * - ConversationTab as default initial route
 * - Each tab maintains its own navigation stack
 *
 * ## Session Management:
 * - Reactively observes Session.account Flow for authentication changes
 * - Automatically switches TopLevelBackStack between Anonymous and authenticated tabs
 * - Ensures stacks are never empty by providing default routes
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

    // Process account changes and update TopLevelBackStack directly
    private val processedAccountFlow = session.account
        .onEach { account ->
            when {
                account.isAuthenticated && topLevelBackStack.topLevelKey == Anonymous -> {
                    Log.d(TAG, "User authenticated, switching to ConversationTab")
                    topLevelBackStack.addTopLevel(ConversationTab)
                    // Ensure ConversationTab has at least itself as entry
                    if (topLevelBackStack.getCurrentStack().isEmpty()) {
                        Log.d(TAG, "ConversationTab stack empty, adding default entry")
                        topLevelBackStack.add(ConversationTab)
                    }
                }

                !account.isAuthenticated && topLevelBackStack.topLevelKey is AuthenticatedTab -> {
                    Log.d(TAG, "User logged out, switching to Anonymous")
                    topLevelBackStack.clearAndSet(Anonymous)
                    topLevelBackStack.add(Login) // Default to Login instead of Welcome
                }
            }
        }

    // Simple reactive state based on current top-level key
    val navigationState: StateFlow<NavigationState> = combine(
        processedAccountFlow,
        snapshotFlow { topLevelBackStack.topLevelKey },
        snapshotFlow { topLevelBackStack.getCurrentStack() }
    ) { account, topLevelKey, currentStack ->
        when {
            account.isAuthenticated && topLevelKey is AuthenticatedTab -> {
                // Ensure authenticated tab has at least itself as entry
                val stack = if (currentStack.isEmpty()) {
                    Log.d(TAG, "Authenticated stack empty, providing default: $topLevelKey")
                    listOf(topLevelKey)
                } else {
                    currentStack
                }

                NavigationState.Authenticated(
                    currentTab = topLevelKey,
                    backStack = stack.mapNotNull { it as? NavigationEntry }
                )
            }

            account.isAuthenticated && topLevelKey == Anonymous -> {
                // User authenticated but still on anonymous stack - transition state
                Log.d(TAG, "Authenticated user on Anonymous stack, showing transition")
                NavigationState.Authenticated(
                    currentTab = ConversationTab,
                    backStack = listOf(ConversationTab)
                )
            }

            !account.isAuthenticated -> {
                // Ensure anonymous stack has at least Login as entry
                val stack = if (currentStack.isEmpty() || currentStack == listOf(Anonymous)) {
                    Log.d(
                        TAG,
                        "Anonymous stack empty or only contains Anonymous, providing default: Login"
                    )
                    listOf(Anonymous, Login)
                } else {
                    currentStack
                }

                NavigationState.Anonymous(
                    backStack = stack.mapNotNull { it as? NavigationEntry }
                )
            }

            else -> NavigationState.Initializing
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
        initialValue = NavigationState.Initializing
    )

    fun authenticate() {
        Log.d(TAG, "User authenticating")
        viewModelScope.launch {
            session.login()
        }
    }

    fun logout() {
        Log.d(TAG, "User logging out")
        viewModelScope.launch {
            session.logout()
        }
    }

    fun navigateToTab(tab: AuthenticatedTab) {
        Log.d(TAG, "Switching to tab: $tab")
        topLevelBackStack.addTopLevel(tab)

        // Ensure the tab has at least itself as entry
        if (topLevelBackStack.getCurrentStack().isEmpty()) {
            Log.d(TAG, "Tab $tab stack empty, adding default entry")
            topLevelBackStack.add(tab)
        }
    }

    fun navigateToEntry(entry: NavigationEntry) {
        Log.d(TAG, "Adding $entry to current stack")
        topLevelBackStack.add(entry)
    }

    fun navigateBack(): Boolean {
        val currentStack = topLevelBackStack.getCurrentStack()
        return if (currentStack.size > 1) {
            Log.d(TAG, "Navigating back")
            topLevelBackStack.removeLast()
            true
        } else {
            Log.d(TAG, "Cannot navigate back - at root of stack")
            false
        }
    }
}
