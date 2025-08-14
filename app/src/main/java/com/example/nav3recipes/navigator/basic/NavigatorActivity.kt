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

package com.example.nav3recipes.navigator.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentPink
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.ui.setEdgeToEdgeConfig

private abstract class NavBarItem(
    val icon: ImageVector,
    val description: String
): Route(isTopLevel = true)
private data object Home : NavBarItem(icon = Icons.Default.Home, description = "Home")
private data object ChatList : NavBarItem(icon = Icons.Default.Face, description = "Chat list")
private data object ChatDetail : Route()
private data object Camera : NavBarItem(icon = Icons.Default.PlayArrow, description = "Camera")
private data object Search : Route(isShared = true)

private val TOP_LEVEL_ROUTES : List<NavBarItem> = listOf(Home, ChatList, Camera)

class NavigatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val navigator = remember { Navigator<Route>(Home) }

            Scaffold(
                topBar = {
                    TopAppBarWithSearch { navigator.navigate(Search) }
                },
                bottomBar = {
                    NavigationBar {
                        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->

                            val isSelected = topLevelRoute == navigator.topLevelRoute
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navigator.navigate(topLevelRoute)
                                },
                                icon = {
                                    Icon(
                                        imageVector = topLevelRoute.icon,
                                        contentDescription = topLevelRoute.description
                                    )
                                }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                NavDisplay(
                    modifier = Modifier.padding(paddingValues),
                    backStack = navigator.backStack,
                    onBack = { navigator.goBack() },
                    entryProvider = entryProvider {
                        entry<Home>{
                            ContentRed("Home screen")
                        }
                        entry<ChatList>{
                            ContentGreen("Chat list screen"){
                                Button(onClick = { navigator.navigate(ChatDetail) }) {
                                    Text("Go to conversation")
                                }
                            }
                        }
                        entry<ChatDetail>{
                            ContentBlue("Chat detail screen")

                        }
                        entry<Camera>{
                            ContentPurple("Camera screen")
                        }
                        entry<Search>{
                            ContentPink("Search screen"){
                                var text by rememberSaveable { mutableStateOf("") }
                                TextField(
                                    value = text,
                                    onValueChange = { newText -> text = newText},
                                    label = { Text("Enter search here") },
                                    singleLine = true
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithSearch(
    onSearchClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text("Navigator Activity")
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            }

        },
    )
}

