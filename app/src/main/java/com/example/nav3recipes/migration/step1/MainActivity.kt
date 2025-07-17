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

package com.example.nav3recipes.migration.step1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * Basic Navigation2 example with two screens. This will be the starting point for migration to
 * Navigation 3.
 */

@Serializable
private data object RouteA

@Serializable
private data class RouteB(val id: String)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val nav3Navigator = remember { Nav3NavigatorSimple(navController) }

            Scaffold { paddingValues ->
                NavDisplay(
                    backStack = nav3Navigator.backStack,
                    entryProvider = entryProvider(fallback = { key ->
                        println("Key $key not handled by entryProvider, using fallback")
                        NavEntry(key = key) {
                            NavHost(
                                navController = navController,
                                startDestination = RouteA,
                                modifier = Modifier.padding(paddingValues)
                            ) {
                                composable<RouteA> {
                                    Column {
                                        Text("Route A")
                                        Button(onClick = { navController.navigate(route = RouteB(id = "123")) }) {
                                            Text("Go to B")
                                        }
                                    }
                                }
                                composable<RouteB> { key ->
                                    Text("Route B: ${key.toRoute<RouteB>().id}")
                                }
                            }
                        }
                    }
                ) {
                    // Empty entryProvider
                })
            }
        }
    }
}

class Nav3NavigatorSimple(val navController: NavHostController){

    val coroutineScope = CoroutineScope(Job())

    // We need a single element to avoid "backStack cannot be empty" error b/430023647
    val backStack = mutableStateListOf<Any>(Unit)

    init {
        coroutineScope.launch {
            navController.currentBackStack.collect { nav2BackStack ->
                with(backStack) {
                    if (nav2BackStack.isNotEmpty()){
                        clear()
                        val entriesToAdd = nav2BackStack.mapNotNull { entry ->
                            // Ignore nav graph root entries
                            if (entry.destination::class.qualifiedName == "androidx.navigation.compose.ComposeNavGraphNavigator.ComposeNavGraph"){
                                null
                            } else {
                                entry
                            }
                        }
                        addAll(entriesToAdd)
                    }
                }
            }
        }
    }
}

