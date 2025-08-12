package com.example.nav3recipes.modular.hilt

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.navigator.LocalNavBackStack
import com.example.nav3recipes.navigator.Route

@Composable
internal fun LoginScreen(
    modifier: Modifier = Modifier,
) {
    val navBackStack = LocalNavBackStack.current
    val viewModel = hiltViewModel<AuthViewModel>()
    ContentBlue(title = "Login Screen", modifier = modifier) {
        Column {
            Button(onClick = {
                viewModel.authenticate()
            }) {
                Text("Sign In")
            }
            Button(onClick = {
                navBackStack.add(Route.Register())
            }) {
                Text("Don't have account? Register")
            }
            Button(onClick = {
                navBackStack.add(Route.ForgotPassword)
            }) {
                Text("Forgot Password?")
            }
        }
    }
}