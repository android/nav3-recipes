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

package com.example.nav3recipes.supportingpane

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable

@Serializable
private data object VideoPlayer : NavKey

@Serializable
private data class Comments(val id: String) : NavKey


/**
 * 2 screens:
 *
 * - Primary - VideoPlayer
 * - Supporting - Comments
 *
 * On mobile:
 * VideoPlayer in single pane
 * Comments should be displayed in a bottom sheet

 * On large screens:
 * VideoPlayer in left pane
 * Comments in right pane
 *
 *
 * Next use the new calculateScene API:
 * - Have a calculateScene that modifies the bottom sheet scene to display it in 2 panes on large screens.
 * - Changes the behavior without needing to change the bottom sheet strategy itself
 */
class SupportingPaneActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberNavBackStack(VideoPlayer)

			val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
			val strategy = SupportingPaneSceneStrategy<NavKey>(windowSizeClass)

			NavDisplay(
                backStack = backStack,
				sceneStrategy = strategy,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<VideoPlayer> {
                        ContentGreen("Video Player") {
                            Button(onClick = {
                                backStack.add(Comments("123"))
                            }) {
                                Text("Click to show comments")
                            }
                        }
                    }
                    entry<Comments>(
						metadata = SupportingPaneSceneStrategy.supportingPane(parent = VideoPlayer.toString())
					) { key ->
                        ContentBlue("Comments for video: ${key.id} ")
                    }
                }
            )
        }
    }
}
