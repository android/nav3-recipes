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

package com.example.nav3recipes.results.saveable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.runtime.result.rememberResultEventBusNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.results.common.Home
import com.example.nav3recipes.results.common.HomeScreen
import com.example.nav3recipes.results.common.Person
import com.example.nav3recipes.results.common.PersonDetailsForm
import com.example.nav3recipes.results.common.PersonDetailsScreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig

/**
 * Reusable extension function on [ResultEventBus] to provide a single [State] that preserves
 * its value across configuration changes and process death using [rememberSaveable].
 * 
 * @param resultKey The unique key associated with this result.
 * @param defaultValue The initial default value of the state. Note that subsequent changes to this
 * argument during recomposition will not overwrite the existing saved state.
 */
@Composable
fun <T> ResultEventBus.conflateAsSaveableState(
    resultKey: String,
    defaultValue: T
): State<T> {
    val savedState = rememberSaveable { mutableStateOf(defaultValue) }
    ResultEffect<T>(resultKey = resultKey, resultEventBus = this) { result ->
        savedState.value = result
    }
    return savedState
}

/**
 * Reified version of [conflateAsSaveableState] using the class name as the key.
 */
@Composable
inline fun <reified T> ResultEventBus.conflateAsSaveableState(
    defaultValue: T
): State<T> = conflateAsSaveableState(T::class.toString(), defaultValue)

class ResultSaveableActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            Scaffold { paddingValues ->
                val backStack = rememberNavBackStack(Home)
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.padding(paddingValues),
                    onBack = { backStack.removeLastOrNull() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberResultEventBusNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<Home> {
                            val resultState = LocalResultEventBus
                                .current
                                .conflateAsSaveableState<Person?>(null)
                            val person = resultState.value
                            HomeScreen(
                                person = person,
                                onNext = { backStack.add(PersonDetailsForm()) }
                            )
                        }
                        entry<PersonDetailsForm> {
                            val resultBus = LocalResultEventBus.current
                            PersonDetailsScreen(
                                onSubmit = { person ->
                                    resultBus.sendResult(result = person)
                                    backStack.removeLastOrNull()
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
