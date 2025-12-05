/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nav3recipes.conditional

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentYellow
import kotlinx.serialization.Serializable


// We use a sealed class for the route supertype because KotlinX Serialization handles polymorphic
// serialization of sealed classes automatically.
@Serializable
sealed class Route(val requiresLogin: Boolean = false) : NavKey

@Serializable
private data object Home : Route()

@Serializable
private data object Profile : Route(requiresLogin = true)

@Serializable
private data object Login : Route()

class ConditionalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val backStack = rememberNavBackStack<Route>(Home)
            val isLoggedInState = rememberSaveable {
                mutableStateOf(false)
            }

            val navigator = Navigator(
                backStack = backStack,
                loginRoute = Login,
                isLoggedInState = isLoggedInState
            )

            NavDisplay(
                backStack = backStack,
                onBack = { navigator.goBack() },
                entryProvider = entryProvider {
                    entry<Home> {
                        ContentGreen("Welcome to Nav3. Logged in? ${isLoggedInState.value}") {
                            Column {
                                Button(onClick = { navigator.navigate(Profile) }) {
                                    Text("Profile")
                                }
                                Button(onClick = { navigator.navigate(Login) }) {
                                    Text("Login")
                                }
                            }
                        }
                    }
                    entry<Profile> {
                        ContentBlue("Profile screen (only accessible once logged in)") {
                            Button(onClick = {
                                navigator.logout()
                            }) {
                                Text("Logout")
                            }
                        }
                    }
                    entry<Login> {
                        ContentYellow("Login screen. Logged in? ${isLoggedInState.value}") {
                            Button(onClick = {
                                navigator.login()
                            }) {
                                Text("Login")
                            }
                        }
                    }
                }
            )
        }
    }
}



// An overload of `rememberNavBackStack` that returns a subtype of `NavKey`.
// If you would like to see this included in the Nav3 library please upvote the following issue:
// https://issuetracker.google.com/issues/463382671
@Composable
fun <T : NavKey> rememberNavBackStack(vararg elements: T): NavBackStack<T> {
    return rememberSerializable(
        serializer = NavBackStackSerializer(elementSerializer = NavKeySerializer())
    ) {
        NavBackStack(*elements)
    }
}