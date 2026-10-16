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

package com.example.nav3recipes.appstate.closed

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appstate.datastore.PersistToDataStore
import androidx.appstate.datastore.StateStorePreferences
import androidx.appstate.datastore.StateStoreSerializer
import androidx.appstate.datastore.syncToDataStore
import androidx.appstate.statestore.StateStore
import androidx.appstate.statestore.StateStoreKey
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentA
import com.example.nav3recipes.content.ContentB
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okio.FileSystem
import okio.Path.Companion.toPath

// Closed polymorphism example
@Serializable
sealed interface AppNavKey : NavKey {
    @Serializable
    data object RouteA : AppNavKey

    @Serializable
    data object RouteB : AppNavKey
}

/**
 * [NavBackStackKey] object to store nav keys in StateStore & DataStore. Since `AppNavKey` is
 * closed via `sealed` keyword, and is marked with `@Serializable` with its children, the
 * serialization in `dataStore` is handled automatically.
 */
private object NavBackStackKey :
    StateStoreKey<NavBackStack<AppNavKey>>(defaultValue = NavBackStack(AppNavKey.RouteA)),
    PersistToDataStore

private lateinit var dataStoreInstance: DataStore<StateStorePreferences>

private val Context.dataStore: DataStore<StateStorePreferences>
    get() = synchronized(this) {
        if (!::dataStoreInstance.isInitialized) {
            val filePath = applicationContext.filesDir
                .resolve("closed_stateStore.preferences_pb")
                .absolutePath
            dataStoreInstance = DataStoreFactory.create(
                storage = OkioStorage(FileSystem.SYSTEM, StateStoreSerializer) {
                    filePath.toPath()
                }
            )
        }
        dataStoreInstance
    }

class AppStateActivity : ComponentActivity() {

    private val stateStore = StateStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            stateStore.syncToDataStore(dataStore)
        }

        setContent {
            val backStack by remember { stateStore.getState(NavBackStackKey) }

            NavDisplay(
                backStack = backStack,
                onBack = {
                    stateStore.updateState(NavBackStackKey) { current ->
                        NavBackStack(*current.dropLast(1).toTypedArray())
                    }
                },
                entryProvider = entryProvider {
                    entry<AppNavKey.RouteA> {
                        ContentA {
                            stateStore.updateState(NavBackStackKey) { current ->
                                NavBackStack(*(current + AppNavKey.RouteB).toTypedArray())
                            }
                        }
                    }
                    entry<AppNavKey.RouteB> {
                        ContentB()
                    }
                }
            )
        }
    }
}
