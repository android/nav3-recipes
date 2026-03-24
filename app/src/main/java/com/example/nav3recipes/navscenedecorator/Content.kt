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

package com.example.nav3recipes.navscenedecorator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.nav3recipes.content.ContentBase
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentMauve
import com.example.nav3recipes.content.ContentOrange
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.scenes.listdetail.ListDetailScene
import com.example.nav3recipes.ui.theme.colors
import kotlinx.serialization.Serializable

@Serializable
data object RouteA : NavKey

@Serializable
data class RouteA1(val id: Int) : NavKey

@Serializable
data object RouteB : NavKey

@Serializable
data object RouteB1 : NavKey

@Serializable
data object RouteC : NavKey

@Serializable
data object RouteC1 : NavKey

const val ITEM_COUNT = 20

fun EntryProviderScope<NavKey>.featureASection(
    onSubRouteClick: (Int) -> Unit,
) {
    entry<RouteA>(
        metadata = ListDetailScene.listPane()
    ) {
        Surface(modifier = Modifier.fillMaxHeight()) {
            var contentPadding by remember { mutableStateOf(PaddingValues()) }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .onWindowInsetsOverlapChanged(WindowInsets.safeDrawing) { contentPadding = it },
                contentPadding = contentPadding
            ) {
                items(List(ITEM_COUNT) { it + 1 }) { id ->
                    ListItem(
                        headlineContent = {
                            Text("Item $id")
                        },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors[id % colors.size])
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = dropUnlessResumed {
                                onSubRouteClick(id)
                            }),
                    )
                }
            }
        }
    }
    entry<RouteA1>(
        metadata = ListDetailScene.detailPane()
    ) { key ->
        ContentBase(
            "Item ${key.id}",
            modifier = Modifier.background(colors[key.id % colors.size])
        ) {
            var count by rememberSaveable {
                mutableIntStateOf(0)
            }

            Button(onClick = { count++ }) {
                Text("Value: $count")
            }
        }
    }
}

fun EntryProviderScope<NavKey>.featureBSection(
    onSubRouteClick: () -> Unit,
) {
    entry<RouteB> {
        ContentGreen("Route B") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = dropUnlessResumed { onSubRouteClick() }) {
                    Text("Go to B1")
                }
            }
        }
    }
    entry<RouteB1> {
        ContentPurple("Route B1") {
            var count by rememberSaveable {
                mutableIntStateOf(0)
            }
            Button(onClick = { count++ }) {
                Text("Value: $count")
            }
        }
    }
}

fun EntryProviderScope<NavKey>.featureCSection(
    onSubRouteClick: () -> Unit,
) {
    entry<RouteC> {
        ContentMauve("Route C") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = dropUnlessResumed { onSubRouteClick() }) {
                    Text("Go to C1")
                }
            }
        }
    }
    entry<RouteC1> {
        ContentOrange("Route C1") {
            var count by rememberSaveable {
                mutableIntStateOf(0)
            }

            Button(onClick = { count++ }) {
                Text("Value: $count")
            }
        }
    }
}

data class NavBarItem(
    val navKey: NavKey,
    val icon: ImageVector,
    val description: String
)

val NAV_ITEMS = listOf(
    NavBarItem(RouteA, Icons.Default.Home, "Route A"),
    NavBarItem(RouteB, Icons.Default.Face, "Route B"),
    NavBarItem(RouteC, Icons.Default.Camera, "Route C"),
)

@Composable
fun NavBar(navBarItems: List<NavBarItem>, navigator: Navigator) {
    NavBar(
        navBarItems = navBarItems,
        topLevelRoute = navigator.state.topLevelRoute,
        onNavItemClick = { navigator.navigate(it) }
    )
}

@Composable
fun NavBar(navBarItems: List<NavBarItem>, topLevelRoute: NavKey, onNavItemClick: (NavKey) -> Unit) {
    NavigationBar(Modifier.consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))) {
        navBarItems.forEach { item ->
            NavigationBarItem(
                selected = item.navKey == topLevelRoute,
                onClick = { onNavItemClick(item.navKey) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.description
                    )
                },
                label = {
                    Text(item.description)
                })
        }
    }
}

@Composable
fun NavRail(navRailItems: List<NavBarItem>, navigator: Navigator) {
    NavRail(
        navRailItems = navRailItems,
        topLevelRoute = navigator.state.topLevelRoute,
        onNavItemClick = { navigator.navigate(it) }
    )
}

@Composable
fun NavRail(
    navRailItems: List<NavBarItem>,
    topLevelRoute: NavKey,
    onNavItemClick: (NavKey) -> Unit
) {
    NavigationRail {
        navRailItems.forEach { item ->
            NavigationRailItem(
                selected = item.navKey == topLevelRoute,
                onClick = { onNavItemClick(item.navKey) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.description
                    )
                },
                label = {
                    Text(item.description)
                })
        }
    }
}