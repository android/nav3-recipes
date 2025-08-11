package com.example.nav3recipes.modular.hilt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val session: Session
) : ViewModel() {

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
        // Restore session if possible
        viewModelScope.launch {
            if (session.isSessionActive()) {
                Log.d(TAG, "Restoring active session")
                _navigationState.value = NavigationState.Authenticated(
                    currentTab = ConversationTab,
                    backStack = tabStacks[ConversationTab]?.toList() ?: listOf(ConversationTab)
                )
            }
        }
    }

    fun authenticate() {
        viewModelScope.launch {
            Log.d(TAG, "User authenticated")
            session.startSession()
            _navigationState.value = NavigationState.Authenticated(
                currentTab = ConversationTab,
                backStack = tabStacks[ConversationTab]?.toList() ?: listOf(ConversationTab)
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "User logged out")
            // Reset tab stacks
            AUTHENTICATED_TABS.forEach { tab ->
                tabStacks[tab] = mutableListOf(tab)
            }
            session.endSession()
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
                _navigationState.value = currentState.copy(
                    currentTab = tab,
                    backStack = tabStacks[tab]?.toList() ?: listOf(tab)
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
                    // Add to current tab's stack
                    val currentTabStack =
                        tabStacks[currentState.currentTab] ?: mutableListOf(currentState.currentTab)
                    val newStack = currentTabStack.toMutableList()
                    newStack.add(entry)
                    tabStacks[currentState.currentTab] = newStack

                    Log.d(TAG, "Adding $entry to tab ${currentState.currentTab}")
                    _navigationState.value = currentState.copy(backStack = newStack)
                }

                is NavigationState.Anonymous -> {
                    // Add to anonymous stack
                    val newStack = currentState.backStack.toMutableList()
                    newStack.add(entry)
                    Log.d(TAG, "Adding $entry to anonymous stack")
                    _navigationState.value = currentState.copy(backStack = newStack)
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

                val currentTabStack = tabStacks[currentState.currentTab] ?: mutableListOf()
                if (currentTabStack.size > 1) {
                    currentTabStack.removeLastOrNull()
                    Log.d(
                        TAG,
                        "Removing from tab ${currentState.currentTab}, remaining: $currentTabStack"
                    )
                    _navigationState.value = currentState.copy(
                        backStack = currentTabStack.toList()
                    )
                    true
                } else {
                    false
                }
            }

            is NavigationState.Anonymous -> {
                if (currentState.backStack.size > 1) {
                    val newStack = currentState.backStack.dropLast(1)
                    Log.d(TAG, "Removing from anonymous stack, remaining: $newStack")
                    _navigationState.value = currentState.copy(backStack = newStack)
                    true
                } else {
                    false
                }
            }
        }
    }
}
