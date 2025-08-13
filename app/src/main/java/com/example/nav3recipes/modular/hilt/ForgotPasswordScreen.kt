package com.example.nav3recipes.modular.hilt

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.navigator.LocalNavBackStack
import com.example.nav3recipes.navigator.Route

@Composable
fun ForgotPasswordScreen(modifier: Modifier = Modifier) {
    val navBackStack = LocalNavBackStack.current
    ContentPurple(title = "Forgot Password Screen", modifier = modifier) {
        Column {
            Button(onClick = {
                navBackStack.add(Route.Login)
            }) {
                Text("Send Reset Email")
            }
            Button(onClick = {
                navBackStack.add(Route.Login)
            }) {
                Text("Back to Login")
            }
        }
    }
}