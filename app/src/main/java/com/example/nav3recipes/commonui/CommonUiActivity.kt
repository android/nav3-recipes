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

package com.example.nav3recipes.commonui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.ui.setEdgeToEdgeConfig

/**
 * Common navigation UI example. This app has three top level routes: Home, ChatList and Camera.
 * ChatList has a sub-route: ChatDetail.
 *
 * The app back stack state is modeled in `TopLevelBackStack`. This creates a back stack for each
 * top level route. It flattens those maps to create a single back stack for `NavDisplay`. This allows
 * `NavDisplay` to know where to go back to.
 *
 * Note that in this example, the Home route can move above the ChatList and Camera routes, meaning
 * navigating back from Home doesn't necessarily leave the app. The app will exit when the user goes
 * back from a single remaining top level route in the back stack.
 */

private sealed interface TopLevelRoute {
    val icon: ImageVector
}
private data object Home : TopLevelRoute { override val icon = Icons.Default.Home }
private data object ChatList : TopLevelRoute { override val icon = Icons.Default.Face }
private data object ChatDetail
private data object Camera : TopLevelRoute { override val icon = Icons.Default.PlayArrow }

private val TOP_LEVEL_ROUTES : List<TopLevelRoute> = listOf(Home, ChatList, Camera)

class CommonUiActivity : ComponentActivity() {

    companion object {
        private const val TAG = "CommonUiActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: Initializing CommonUiActivity")
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val topLevelBackStack = remember { TopLevelBackStack<Any>(Home) }
            Log.d(TAG, "onCreate: TopLevelBackStack initialized with Home route")

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->

                            val isSelected = topLevelRoute == topLevelBackStack.topLevelKey
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    Log.d(TAG, "NavigationBarItem clicked: $topLevelRoute")
                                    topLevelBackStack.addTopLevel(topLevelRoute)
                                },
                                icon = {
                                    Icon(
                                        imageVector = topLevelRoute.icon,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            ) { contentPadding ->
                NavDisplay(
                    backStack = topLevelBackStack.backStack,
                    onBack = {
                        Log.d(TAG, "onBack called")
                        topLevelBackStack.removeLast()
                    },
                    entryProvider = entryProvider {
                        entry<Home>{
                            ContentRed("Home screen")
                        }
                        entry<ChatList>{
                            ContentGreen("Chat list screen"){
                                Button(onClick = {
                                    Log.d(TAG, "Navigate to ChatDetail")
                                    topLevelBackStack.add(ChatDetail)
                                }) {
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
                    },
                )
            }
        }
    }
}

class TopLevelBackStack<T: Any>(startKey: T) {

    companion object {
        private const val TAG = "TopLevelBackStack"
    }

    // Maintain a stack for each top level route
    private var topLevelStacks : LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    // Expose the current top level route for consumers
    var topLevelKey by mutableStateOf(startKey)
        private set

    // Expose the back stack so it can be rendered by the NavDisplay
    val backStack = mutableStateListOf(startKey)

    private fun updateBackStack(): SnapshotStateList<T> {
        Log.d(TAG, "updateBackStack: Updating back stack")
        return backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
            Log.d(TAG, "updateBackStack: New back stack size: ${this.size}, contents: $this")
        }
    }

    fun addTopLevel(key: T){
        Log.d(TAG, "addTopLevel: Adding top level route: $key")

        // If the top level doesn't exist, add it
        if (topLevelStacks[key] == null){
            Log.d(TAG, "addTopLevel: Creating new stack for $key")
            topLevelStacks.put(key, mutableStateListOf(key))
        } else {
            Log.d(TAG, "addTopLevel: Moving existing stack for $key to end")
            // Otherwise just move it to the end of the stacks
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        }
        topLevelKey = key
        Log.d(TAG, "addTopLevel: Current top level key: $topLevelKey")
        updateBackStack()
    }

    fun add(key: T){
        Log.d(TAG, "add: Adding $key to current top level stack ($topLevelKey)")
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast(){
        Log.d(TAG, "removeLast: Removing last item from current stack ($topLevelKey)")
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()
        Log.d(TAG, "removeLast: Removed key: $removedKey")

        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        if (removedKey != null) {
            Log.d(TAG, "removeLast: Removed top level stack for: $removedKey")
        }

        topLevelKey = topLevelStacks.keys.last()
        Log.d(TAG, "removeLast: New top level key: $topLevelKey")
        updateBackStack()
    }
}

