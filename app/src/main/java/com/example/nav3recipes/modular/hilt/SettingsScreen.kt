package com.example.nav3recipes.modular.hilt

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nav3recipes.content.ContentRed

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val viewModel = hiltViewModel<AuthViewModel>()
    ContentRed(title="Settings Screen", modifier = modifier) {
        Column {
            Button(onClick = {
                viewModel.logout()
            }) {
                Text("Logout")
            }
        }
    }
}