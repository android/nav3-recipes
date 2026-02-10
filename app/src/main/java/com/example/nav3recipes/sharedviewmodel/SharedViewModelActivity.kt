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

package com.example.nav3recipes.sharedviewmodel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.sharedviewmodel.SharedViewModelStoreNavEntryDecorator.Companion.viewModelStoreKey
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable


@Serializable
private data object ParentScreen : NavKey

@Serializable
private data object ChildScreen : NavKey

@Serializable
private data object StandaloneScreen : NavKey

class SharedViewModelActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            val backStack = rememberNavBackStack(ParentScreen)

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberSharedViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<ParentScreen>(
                        clazzContentKey = { key -> key.toContentKey() },
                    ) {
                        val viewModel = viewModel(modelClass = CounterViewModel::class)

                        ContentRed("Parent screen") {
                            Button(onClick = dropUnlessResumed { viewModel.count++ }) {
                                Text("Count: ${viewModel.count}")
                            }
                            Button(onClick = dropUnlessResumed { backStack.add(ChildScreen) }) {
                                Text("View child screen")
                            }
                        }
                    }
                    entry<ChildScreen>(
                        metadata = metadata {
                            viewModelStoreKey(ParentScreen.toContentKey())
                        }
                    ) {
                        val parentViewModel = viewModel(modelClass = CounterViewModel::class)

                        ContentBlue("Child screen") {
                            Button(onClick = dropUnlessResumed { parentViewModel.count++ }) {
                                Text("Parent count: ${parentViewModel.count}")
                            }
                            Button(onClick = dropUnlessResumed {
                                backStack.add(StandaloneScreen)
                            }) {
                                Text("View standalone screen")
                            }
                        }
                    }
                    entry<StandaloneScreen> {
                        val viewModel = viewModel(modelClass = CounterViewModel::class)

                        ContentGreen("Standalone screen") {
                            Button(onClick = dropUnlessResumed {
                                viewModel.count++
                            }) {
                                Text("Count: ${viewModel.count}")
                            }
                        }
                    }
                }
            )
        }
    }
}

fun NavKey.toContentKey() = this.toString()

class CounterViewModel : ViewModel() {
    var count by mutableIntStateOf(0)
}
