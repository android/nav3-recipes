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

package com.example.nav3recipes.migration.step6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentMauve
import com.example.nav3recipes.content.ContentPink
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
data object BaseRouteA
@Serializable
data object RouteA : Route.TopLevel
@Serializable
data object RouteA1

@Serializable
data object BaseRouteB
@Serializable
data object RouteB : Route.TopLevel
@Serializable
data class RouteB1(val id: String)

@Serializable
data object BaseRouteC
@Serializable
data object RouteC : Route.TopLevel
@Serializable
data object RouteD
@Serializable
data object RouteE : Route.Shared

private val TOP_LEVEL_ROUTES = mapOf(
    BaseRouteA to NavBarItem(icon = Icons.Default.Home, description = "Route A"),
    BaseRouteB to NavBarItem(icon = Icons.Default.Face, description = "Route B"),
    BaseRouteC to NavBarItem(icon = Icons.Default.Camera, description = "Route C"),
)

data class NavBarItem(
    val icon: ImageVector,
    val description: String
)


class Step6MigrationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val coroutineScope = rememberCoroutineScope()
            val navController = rememberNavController()
            val navigator = remember { Navigator(coroutineScope, navController) }
            val currentBackStackEntry by navController.currentBackStackEntryAsState()

            Scaffold(bottomBar = {
                NavigationBar {
                    TOP_LEVEL_ROUTES.forEach { (key, value) ->
                        val isSelected =
                            currentBackStackEntry?.destination.isRouteInHierarchy(key::class)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(key, navOptions {
                                    popUpTo(route = RouteA)
                                })
                            },
                            icon = {
                                Icon(
                                    imageVector = value.icon,
                                    contentDescription = value.description
                                )
                            },
                            label = { Text(value.description) }
                        )
                    }
                }
            })

            { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    // Base Layer: Legacy NavHost is always in the composition tree.
                    NavHost(
                        navController = navController,
                        startDestination = BaseRouteA
                    ) {
                        // All routes are now rendered by NavDisplay, so these are all empty.
                        navigation<BaseRouteA>(startDestination = RouteA) {
                            composable<RouteA> {}
                            composable<RouteA1> {}
                            composable<RouteE> {}
                        }
                        navigation<BaseRouteB>(startDestination = RouteB) {
                            composable<RouteB> {}
                            composable<RouteB1> {}
                            composable<RouteE> {}
                        }
                        navigation<BaseRouteC>(startDestination = RouteC) {
                            composable<RouteC> {}
                            composable<RouteE> {}
                        }
                        dialog<RouteD> {}
                    }

                    // Overlay Layer: NavDisplay for all screens.
                    NavDisplay(
                        backStack = navigator.backStack,
                        onBack = { navigator.goBack() },
                        sceneStrategy = remember { DialogSceneStrategy() },
                        entryProvider = entryProvider(
                            fallback = { key ->
                                // Should ideally not be called if all routes are migrated.
                                NavEntry(key = key) {}
                            }
                        ) {
                            featureASection(
                                onSubRouteClick = { navigator.navigate(RouteA1) },
                                onDialogClick = { navigator.navigate(RouteD) },
                                onOtherClick = { navigator.navigate(RouteE) }
                            )
                            featureBSection(
                                onDetailClick = { id -> navigator.navigate(RouteB1(id)) },
                                onDialogClick = { navigator.navigate(RouteD) },
                                onOtherClick = { navigator.navigate(RouteE) }
                            )
                            featureCSection(
                                onDialogClick = { navigator.navigate(RouteD) },
                                onOtherClick = { navigator.navigate(RouteE) }
                            )
                            entry<RouteD>(metadata = DialogSceneStrategy.dialog()) {
                                Text(
                                    modifier = Modifier.background(Color.White),
                                    text = "Route D title (dialog)"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun EntryProviderBuilder<Any>.featureASection(
    onSubRouteClick: () -> Unit,
    onDialogClick: () -> Unit,
    onOtherClick: () -> Unit,
) {
    entry<RouteA> {
        ContentRed("Route A title") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = onSubRouteClick) {
                    Text("Go to A1")
                }
                Button(onClick = onDialogClick) {
                    Text("Open dialog D")
                }
                Button(onClick = onOtherClick) {
                    Text("Go to E")
                }
            }
        }
    }
    entry<RouteA1> { ContentPink("Route A1 title") }
    entry<RouteE> { ContentBlue("Route E title") }
}

private fun EntryProviderBuilder<Any>.featureBSection(
    onDetailClick: (id: String) -> Unit,
    onDialogClick: () -> Unit,
    onOtherClick: () -> Unit
) {
    entry<RouteB> {
        ContentGreen("Route B title") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { onDetailClick("ABC") }) {
                    Text("Go to B1")
                }
                Button(onClick = onDialogClick) {
                    Text("Open dialog D")
                }
                Button(onClick = onOtherClick) {
                    Text("Go to E")
                }
            }
        }
    }
    entry<RouteB1> { key ->
        ContentPurple("Route B1 title. ID: ${key.id}")
    }
}

private fun EntryProviderBuilder<Any>.featureCSection(
    onDialogClick: () -> Unit,
    onOtherClick: () -> Unit,
) {
    entry<RouteC> {
        ContentMauve("Route C title") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = onDialogClick) {
                    Text("Open dialog D")
                }
                Button(onClick = onOtherClick) {
                    Text("Go to E")
                }
            }
        }
    }
}

private fun NavDestination?.isRouteInHierarchy(route: KClass<*>) =
    this?.hierarchy?.any {
        it.hasRoute(route)
    } ?: false