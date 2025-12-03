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

package com.example.nav3recipes.scenes.threepane

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.example.nav3recipes.scenes.threepane.ThreePaneScene.Companion.THREE_PANE_KEY
import com.example.nav3recipes.scenes.twopane.TwoPaneScene
import com.example.nav3recipes.scenes.twopane.TwoPaneScene.Companion.TWO_PANE_KEY


// --- ThreePaneScene ---
/**
 * A custom [Scene] that displays three [NavEntry]s side-by-side.
 */
class ThreePaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val firstEntry: NavEntry<T>,
    val secondEntry: NavEntry<T>,
    val thirdEntry: NavEntry<T>
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(firstEntry, secondEntry, thirdEntry)
    override val content: @Composable (() -> Unit) = {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                firstEntry.Content()
            }
            Column(modifier = Modifier.weight(1f)) {
                secondEntry.Content()
            }
            Column(modifier = Modifier.weight(1f)) {
                thirdEntry.Content()
            }
        }
    }

    companion object {
        internal const val THREE_PANE_KEY = "ThreePane"
        /**
         * Helper function to add metadata to a [NavEntry] indicating it can be displayed
         * in a three-pane layout.
         */
        fun threePane() = mapOf(THREE_PANE_KEY to true)
    }
}

@Composable
fun <T: Any> rememberThreePaneSceneStrategy() : ThreePaneSceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo(true).windowSizeClass

    return remember(windowSizeClass){
        ThreePaneSceneStrategy(windowSizeClass)
    }
}

// --- ThreePaneSceneStrategy ---
/**
 * A [SceneStrategy] that activates a [ThreePaneScene] if the window is wide enough
 * and the top three back stack entries declare support for three-pane display.
 * It will default to a [TwoPaneScene] if the window is between 600dp and 1200dp
 */
class ThreePaneSceneStrategy<T : Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        // Try for three-pane first on large screens
        if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND)) {
            val lastThree = entries.takeLast(3)
            if (lastThree.size == 3 && lastThree.all { it.metadata.containsKey(THREE_PANE_KEY) }) {
                val firstEntry = lastThree[0]
                val secondEntry = lastThree[1]
                val thirdEntry = lastThree[2]

                val sceneKey = Triple(firstEntry.contentKey, secondEntry.contentKey, thirdEntry.contentKey)

                return ThreePaneScene(
                    key = sceneKey,
                    previousEntries = entries.dropLast(3),
                    firstEntry = firstEntry,
                    secondEntry = secondEntry,
                    thirdEntry = thirdEntry
                )
            }
        }

        // Fallback to two-pane on medium screens (or large screens if 3-pane not applicable)
        if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            val lastTwo = entries.takeLast(2)
            if (lastTwo.size == 2 && lastTwo.all {
                    it.metadata.containsKey(THREE_PANE_KEY) ||
                            it.metadata.containsKey(TWO_PANE_KEY)
                }) {
                // we can assume that if the entries have declared that they can be displayed
                // in a three pane scene, they can also be displayed in a 2 pane scene

                val firstEntry = lastTwo.first()
                val secondEntry = lastTwo.last()

                val sceneKey = Pair(firstEntry.contentKey, secondEntry.contentKey)

                return TwoPaneScene(
                    key = sceneKey,
                    previousEntries = entries.dropLast(2),
                    firstEntry = firstEntry,
                    secondEntry = secondEntry
                )
            }
        }

        return null
    }
}

