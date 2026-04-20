/*
 * Copyright 2026 The Android Open Source Project
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

package com.example.nav3recipes.basicparcelable

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.parcelize.Parcelize

sealed interface Route : Parcelable

@Parcelize
data object RouteA : Route

@Parcelize
data class RouteB(val id: String) : Route

/**
 * Creates and remembers a [SnapshotStateList] to hold a back stack of [Parcelable] routes
 * that survives configuration changes and process death.
 *
 * @param T The route type, which must implement [Parcelable].
 * @param elements The initial routes to populate the back stack.
 * @return A reactive [SnapshotStateList] managing the navigation back stack.
 */
@Composable
fun <T : Parcelable> rememberParcelableBackStack(vararg elements: T): SnapshotStateList<T> {
    return rememberSaveable {
        mutableStateListOf(*elements)
    }
}

class BasicParcelableActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberParcelableBackStack<Route>(RouteA)

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = { key ->
                    when (key) {
                        is RouteA -> NavEntry(key) {
                            ContentGreen("Welcome to Nav3") {
                                Button(onClick = dropUnlessResumed {
                                    backStack.add(RouteB("123"))
                                }) {
                                    Text("Click to navigate")
                                }
                            }
                        }

                        is RouteB -> NavEntry(key) {
                            ContentBlue("Route id: ${key.id} ")
                        }
                    }
                }
            )
        }
    }
}
