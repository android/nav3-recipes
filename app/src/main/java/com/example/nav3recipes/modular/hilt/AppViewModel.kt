package com.example.nav3recipes.modular.hilt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    session: Session
) : ViewModel() {
    val sessionState: StateFlow<SessionState> = session.account
        .map { SessionState.Initialized(it) }
        .stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = SessionState.Initializing
        )
}
