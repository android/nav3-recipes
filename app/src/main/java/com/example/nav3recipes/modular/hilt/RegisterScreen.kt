package com.example.nav3recipes.modular.hilt

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nav3recipes.content.ContentYellow
import com.example.nav3recipes.navigator.LocalNavBackStack
import com.example.nav3recipes.navigator.Route

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    email: String?
) {
    val navBackStack = LocalNavBackStack.current
    val viewModel = hiltViewModel<AuthViewModel>()
    ContentYellow(title = "Register Screen", modifier = modifier) {
        Column {
            if (!email.isNullOrBlank()) {
                Text(text = "received email from deeplink: $email")
            }
            Button(onClick = {
                viewModel.authenticate()
            }) {
                Text("Create Account")
            }
            Button(onClick = {
                navBackStack.add(Route.Login)
            }) {
                Text("Already have account? Login")
            }
        }
    }
}