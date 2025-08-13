package com.example.nav3recipes.modular.hilt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val session: Session
) : ViewModel() {
    fun authenticate() {
        viewModelScope.launch {
            session.login()
        }
    }

    fun logout() {
        viewModelScope.launch {
            session.logout()
        }
    }
}
