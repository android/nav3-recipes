package com.example.nav3recipes.material.listdetail

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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.content.ContentYellow
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable

/**
 * This example uses the Material ListDetailSceneStrategy to create an adaptive scene. It has three
 * destinations: ConversationList, ConversationDetail and Profile. When the window width allows it,
 * the content for these destinations will be shown in a two pane layout.
 */
@Serializable
private object ConversationList : NavKey

@Serializable
private data class ConversationDetail(val id: String) : NavKey

@Serializable
private data object Profile : NavKey

class MaterialListDetailActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {

            val backStack = rememberNavBackStack(ConversationList)

            // Override the defaults so that there isn't a horizontal space between the panes.
            // See b/418201867
            val windowAdaptiveInfo = currentWindowAdaptiveInfo()
            val directive = remember(windowAdaptiveInfo) {
                calculatePaneScaffoldDirective(windowAdaptiveInfo)
                    .copy(horizontalPartitionSpacerSize = 0.dp)
            }
            val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

            NavDisplay(
                backStack = backStack,
                onBack = { keysToRemove -> repeat(keysToRemove) { backStack.removeLastOrNull() } },
                sceneStrategy = listDetailStrategy,
                entryProvider = entryProvider {
                    entry<ConversationList>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = {
                                ContentYellow("Choose a conversation from the list")
                            }
                        )
                    ) {
                        ContentRed("Welcome to Nav3") {
                            Button(onClick = {
                                backStack.add(ConversationDetail("ABC"))
                            }) {
                                Text("View conversation")
                            }
                        }
                    }
                    entry<ConversationDetail>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) { conversation ->
                        ContentBlue("Conversation ${conversation.id} ") {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(onClick = {
                                    backStack.add(Profile)
                                }) {
                                    Text("View profile")
                                }
                            }
                        }
                    }
                    entry<Profile>(
                        metadata = ListDetailSceneStrategy.extraPane()
                    ) {
                        ContentGreen("Profile")
                    }
                }
            )
        }
    }
}
