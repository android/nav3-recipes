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

package com.example.nav3recipes.migration.step7

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderBuilder
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


@Serializable
data object RouteA : Route.TopLevel()
@Serializable
data object RouteA1 : Route()

@Serializable
data object RouteB : Route.TopLevel()
@Serializable
data class RouteB1(val id: String) : Route()

@Serializable
data object RouteC : Route.TopLevel()
@Serializable
data object RouteD : Route()
@Serializable
data object RouteE : Route.Shared()

private val TOP_LEVEL_ROUTES = mapOf<Route, NavBarItem>(
    RouteA to NavBarItem(icon = Icons.Default.Home, description = "Route A"),
    RouteB to NavBarItem(icon = Icons.Default.Face, description = "Route B"),
    RouteC to NavBarItem(icon = Icons.Default.Camera, description = "Route C"),
)

data class NavBarItem(
    val icon: ImageVector,
    val description: String
)


class Step7MigrationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {

            val navigator = rememberNavigator(startRoute = RouteA, shouldPrintDebugInfo = true)

            Scaffold(bottomBar = {
                NavigationBar {
                    TOP_LEVEL_ROUTES.forEach { (key, value) ->
                        val isSelected = key == navigator.topLevelRoute
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navigator.navigate(key)
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
                NavDisplay(
                    backStack = navigator.backStack,
                    onBack = { navigator.goBack() },
                    sceneStrategy = remember { DialogSceneStrategy() },
                    entryProvider = entryProvider {
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
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

private fun EntryProviderBuilder<Route>.featureASection(
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

private fun EntryProviderBuilder<Route>.featureBSection(
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

private fun EntryProviderBuilder<Route>.featureCSection(
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
