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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.lifecycle.compose.dropUnlessResumed
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


/**
 * Class for representing navigation keys in the app.
 *
 * Note: We use a sealed class because KotlinX Serialization handles
 * polymorphic serialization of sealed classes automatically.
 *
 * @param requiresLogin - true if the navigation key requires that the user is logged in
 * to navigate to it
 */
@Serializable
sealed class ConditionalNavKey(val requiresLogin: Boolean = false) : NavKey

/**
 * Key representing home screen
 */
@Serializable
private data object Home : ConditionalNavKey()

/**
 * Key representing profile screen that is only accessible once the user has logged in
 */
@Serializable
private data object Profile : ConditionalNavKey(requiresLogin = true)

/**
 * Key representing login screen
 *
 * @param redirectToKey - navigation key to redirect to after successful login
 */
@Serializable
private data class Login(
    val redirectToKey: ConditionalNavKey? = null
) : ConditionalNavKey()

class ConditionalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val backStack = rememberNavBackStack<ConditionalNavKey>(Home)
            var isLoggedIn by rememberSaveable {
                mutableStateOf(false)
            }
            val navigator = remember {
                Navigator(
                    backStack = backStack,
                    onNavigateToRestrictedKey = { redirectToKey -> Login(redirectToKey) },
                    isLoggedIn = { isLoggedIn }
                )
            }

            NavDisplay(
                backStack = backStack,
                onBack = { navigator.goBack() },
                entryProvider = entryProvider {
                    entry<Home> {
                        ContentGreen("Welcome to Nav3. Logged in? ${isLoggedIn}") {
                            Column {
                                Button(onClick = dropUnlessResumed { navigator.navigate(Profile) }) {
                                    Text("Profile")
                                }
                                Button(onClick = dropUnlessResumed { navigator.navigate(Login()) }) {
                                    Text("Login")
                                }
                            }
                        }
                    }
                    entry<Profile> {
                        ContentBlue("Profile screen (only accessible once logged in)") {
                            Button(onClick = dropUnlessResumed {
                                isLoggedIn = false
                                navigator.navigate(Home)
                            }) {
                                Text("Logout")
                            }
                        }
                    }
                    entry<Login> { key ->
                        ContentYellow("Login screen. Logged in? $isLoggedIn") {
                            Button(onClick = dropUnlessResumed {
                                isLoggedIn = true
                                key.redirectToKey?.let { targetKey ->
                                    backStack.remove(key)
                                    navigator.navigate(targetKey)
                                }
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
// See https://issuetracker.google.com/issues/463382671 for a discussion of this function
@Composable
fun <T : NavKey> rememberNavBackStack(vararg elements: T): NavBackStack<T> {
    return rememberSerializable(
        serializer = NavBackStackSerializer(elementSerializer = NavKeySerializer())
    ) {
        NavBackStack(*elements)
    }
}