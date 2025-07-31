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

package com.example.nav3recipes.scenes.threepanes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.SceneStrategy
import androidx.navigation3.ui.SinglePaneSceneStrategy
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentPink
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import com.example.nav3recipes.ui.theme.colors
import kotlinx.serialization.Serializable

/**
 * Basic example with two screens that uses the entryProvider DSL and has a persistent back stack.
 */

@Serializable
private data object RouteA : NavKey

@Serializable
private data class RouteB(val id: Int) : NavKey {
    val color: Color
        get() = colors[id % colors.size]
}

@Serializable
private data class RouteC(val id: String) : NavKey

class ThreePanesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberNavBackStack(RouteA)
            val info = currentWindowAdaptiveInfo(true).windowSizeClass

            val combinedStrategy: SceneStrategy<NavKey> = ThreePaneSceneStrategy<NavKey>(info).then(
                DualPaneSceneStrategy<NavKey>(info)
            ).then(
                SinglePaneSceneStrategy()
            )


            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategy = combinedStrategy,
                entryProvider = entryProvider {
                    entry<RouteA>(metadata = mapOf(

                    )) {
                        MainList { route ->
                            backStack.add(route)
                        }
                    }
                    entry<RouteB> { key ->
                        SubList(key) { route ->
                            backStack.add(route)
                        }
                    }
                    entry<RouteC> { key ->
                        ContentPink("Route id: ${key.id}")
                    }
                }
            )
        }
    }
}

@Composable
private fun MainList(onItemClicked: (NavKey) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(10) { index ->
            val sublistId = index + 1
            val route = RouteB(sublistId)
            val backgroundColor = route.color
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onItemClicked(route) }),
                headlineContent = {
                    Text(
                        text = "Conversation $sublistId",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = backgroundColor // Set container color directly
                )
            )
        }
    }
}

@Composable
private fun SubList(parentKey: RouteB, onItemClicked: (NavKey) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(10) { index ->
            val detailId = "${parentKey.id}${index + 1}"
            val route = RouteC(detailId)
            val backgroundColor = parentKey.color
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor.copy(alpha = (index / 10).toFloat()))
                    .clickable(onClick = { onItemClicked(route) }),
                headlineContent = {
                    Text(
                        text = "Conversation $detailId",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = backgroundColor // Set container color directly
                )
            )
        }
    }
}
