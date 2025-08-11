package com.example.nav3recipes.modular.hilt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val session: Session
) : ViewModel() {
    companion object {
        private const val TAG = "NavigationViewModel"
    }

    // Process account changes and update TopLevelBackStack directly
    val sessionState: StateFlow<SessionState> = session.account
        .map { SessionState.Initialized(it) }
        .stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = SessionState.Initializing
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
}
