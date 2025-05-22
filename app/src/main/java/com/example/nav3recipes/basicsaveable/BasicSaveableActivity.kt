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

package com.example.nav3recipes.basicsaveable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.ui.enableEdgeToEdgeCompat
import kotlinx.serialization.Serializable

/**
 * Basic example with a persistent back stack state.
 *
 * The back stack persists config changes because it's created using `rememberNavBackStack`. This
 * requires that the back stack keys be both serializable and implement `NavKey`.
 */

@Serializable
data object RouteA : NavKey

@Serializable
data class RouteB(val id: String) : NavKey

class BasicSaveableActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdgeCompat()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberNavBackStack(RouteA)

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = { key ->
                    when (key) {
                        is RouteA -> NavEntry(key) {
                            ContentGreen("Welcome to Nav3") {
                                Button(onClick = {
                                    backStack.add(RouteB("123"))
                                }) {
                                    Text("Click to navigate")
                                }
                            }
                        }

                        is RouteB -> NavEntry(key) {
                            ContentBlue("Route id: ${key.id} ")
                        }

                        else -> {
                            error("Unknown route: $key")
                        }
                    }
                }
            )
        }
    }
}
