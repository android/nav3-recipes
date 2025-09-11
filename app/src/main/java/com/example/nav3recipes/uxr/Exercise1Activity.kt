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

package com.example.nav3recipes.uxr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.Scene
import androidx.navigation3.ui.SceneStrategy
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.example.nav3recipes.content.ContentBase
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import com.example.nav3recipes.ui.theme.Grey20
import com.example.nav3recipes.ui.theme.Grey80
import com.example.nav3recipes.ui.theme.Grey90
import com.example.nav3recipes.ui.theme.PastelYellow
import com.example.nav3recipes.ui.theme.colors
import kotlinx.serialization.Serializable

/**
 * Scenes UXR Study Exercise
 */

@Serializable
object ConversationList : NavKey

@Serializable
data class ConversationDetail(val id: Int) : NavKey {
    val color: Color
        get() = colors[id % colors.size]
}

class Exercise1Activity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberNavBackStack<NavKey>(ConversationList)

            val twoPaneSceneStrategy = remember { VerticalListDetailSceneStrategy<NavKey>() }

            // Override the defaults so that there isn't a horizontal space between the panes.
            val windowAdaptiveInfo = currentWindowAdaptiveInfo()
            val directive = remember(windowAdaptiveInfo) {
                calculatePaneScaffoldDirective(windowAdaptiveInfo)
                    .copy(horizontalPartitionSpacerSize = 0.dp)
            }
            val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

            Scaffold { paddingValues ->
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.padding(paddingValues),
                    sceneStrategy = twoPaneSceneStrategy then listDetailStrategy,
                    entryProvider = entryProvider {
                        entry<ConversationList>(metadata =
                            ListDetailSceneStrategy.listPane(detailPlaceholder = {
                                ContentBase(
                                    title = "Choose a conversation from the list",
                                    modifier = Modifier.background(Grey90)
                                ) }) + mapOf("list" to true)
                        ) {
                            ConversationListScreen(
                                onConversationClicked = { conversationDetail ->
                                    // Pop any existing ConversationDetail screens
                                    backStack.removeIf { it is ConversationDetail }
                                    backStack.add(conversationDetail)
                                }
                            )
                        }
                        entry<ConversationDetail>(
                            metadata = ListDetailSceneStrategy.detailPane() + mapOf("detail" to true)
                        ) { key ->
                            ConversationDetailScreen(key)
                        }
                    }
                )
            }
        }
    }
}

class VerticalListDetailSceneStrategy<T: Any> : SceneStrategy<T> {
    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (Int) -> Unit
    ): Scene<T>? {

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

        if (!windowSizeClass.isHeightAtLeastBreakpoint(600)) {
            return null
        }

        val detailEntry = entries.findLast { it.metadata.containsKey("detail") }
        val listEntry = entries.findLast { it.metadata.containsKey("list") }

        return if (listEntry != null && detailEntry != null){

            VerticalListDetailScene(
                listEntry = listEntry,
                detailEntry = detailEntry,
                previousEntries = entries.dropLast(2)
            )
        } else {
            null
        }
    }
}

class VerticalListDetailScene<T: Any>(
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,

) : Scene<T> {

    override val key = listEntry.contentKey
    override val entries: List<NavEntry<T>> = listOf(listEntry, detailEntry)

    override val content: @Composable (() -> Unit) = {
        Column {
            Row(modifier = Modifier.weight(0.5f)) {
                listEntry.Content()
            }
            Row(modifier = Modifier.weight(0.5f)) {
                detailEntry.Content()
            }
        }
    }

}