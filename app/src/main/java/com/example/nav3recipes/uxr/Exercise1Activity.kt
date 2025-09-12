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
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.navEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.LocalNavAnimatedContentScope
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

    @OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberNavBackStack<NavKey>(ConversationList)

            // Override the defaults so that there isn't a horizontal space between the panes.
            val windowAdaptiveInfo = currentWindowAdaptiveInfo()
            val directive = remember(windowAdaptiveInfo) {
                calculatePaneScaffoldDirective(windowAdaptiveInfo)
                    .copy(horizontalPartitionSpacerSize = 0.dp)
            }
            val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)
            val verticalListDetailStrategy = remember { VerticalListDetailSceneStrategy<NavKey>() }
            val singlePaneStrategy = remember { SinglePaneSceneStrategy<NavKey>() }

            Scaffold { paddingValues ->
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.padding(paddingValues),
                    sceneStrategy =
                        listDetailStrategy then
                        verticalListDetailStrategy then
                        singlePaneStrategy,
                    entryProvider = entryProvider {
                        entry<ConversationList>(
                            metadata =
                                ListDetailSceneStrategy.listPane(detailPlaceholder = {
                                    ContentBase(
                                        title = "Choose a conversation from the list",
                                        modifier = Modifier.background(Grey90)
                                    )
                                }) + mapOf("list" to true) + mapOf(
                                    SinglePaneSceneStrategy.TITLE_KEY to "Conversation list"
                                )
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
                            metadata = ListDetailSceneStrategy.detailPane() + mapOf("detail" to true) + mapOf(
                                SinglePaneSceneStrategy.TITLE_KEY to "Conversation detail"
                            )
                        ) { key ->
                            ConversationDetailScreen(key)
                        }
                    }
                )
            }
        }
    }
}

class SinglePaneSceneStrategy<T : Any> : SceneStrategy<T> {
    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (Int) -> Unit
    ): Scene<T>? {
        return SinglePaneScene(entry = entries.last(), previousEntries = entries.dropLast(1))
    }

    companion object {
        const val TITLE_KEY = "single_pane_title"
    }
}

class SinglePaneScene<T : Any>(
    entry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>
) : Scene<T> {

    override val key = entry.contentKey
    override val entries = listOf(entry)

    @OptIn(ExperimentalMaterial3Api::class)
    override val content: @Composable (() -> Unit) = {

        val title: String =
            entry.metadata[SinglePaneSceneStrategy.TITLE_KEY] as? String ?: "Unknown title"

        Column {
            Row {
                CenterAlignedTopAppBar(title = {
                    Text(title)
                })
            }
            Row {
                entry.Content()
            }
        }
    }
}

class VerticalListDetailSceneStrategy<T : Any> : SceneStrategy<T> {
    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (Int) -> Unit
    ): Scene<T>? {

        var scene : Scene<T>? = null
        val windowInfo = LocalWindowInfo.current

        if (windowInfo.containerSize.height > windowInfo.containerSize.width) {
            val detailEntry = entries.findLast { it.metadata.containsKey("detail") }
            val listEntry = entries.findLast { it.metadata.containsKey("list") }

            if (listEntry != null && detailEntry != null) {
                scene = VerticalListDetailScene(
                    listEntry = listEntry,
                    detailEntry = detailEntry,
                    previousEntries = entries.dropLast(1)
                )
            }
        }
        return scene
    }
}

class VerticalListDetailScene<T : Any>(
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