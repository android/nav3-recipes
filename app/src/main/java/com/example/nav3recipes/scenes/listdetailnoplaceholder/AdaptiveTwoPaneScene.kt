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

@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.nav3recipes.scenes.listdetailnoplaceholder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

internal class AdaptiveThreePaneScene<T : Any>(
    val firstPane: NavEntry<T>,
    val secondPane: NavEntry<T>,
    val thirdPane: NavEntry<T>,
    val weights: ListDetailNoPlaceholderSceneStrategy.SceneDefaults,
    override val previousEntries: List<NavEntry<T>>,
    override val key: Any
) : Scene<T> {

    override val entries: List<NavEntry<T>> = listOf(firstPane, secondPane, thirdPane)

    override val content: @Composable (() -> Unit) = {

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(weights.threePanesSceneFirstPaneWeight)) {
                firstPane.Content()
            }
            Column(modifier = Modifier.weight(weights.threePanesSceneSecondPaneWeight)) {
                secondPane.Content()
            }
            Column(modifier = Modifier.weight(weights.threePanesSceneThirdPaneWeight)) {
                thirdPane.Content()
            }
        }
    }
}

internal class AdaptiveTwoPaneScene<T : Any>(
    val firstPane: NavEntry<T>,
    val secondPane: NavEntry<T>,
    val weights: ListDetailNoPlaceholderSceneStrategy.SceneDefaults,
    override val previousEntries: List<NavEntry<T>>,
    override val key: Any
) : Scene<T> {

    override val entries: List<NavEntry<T>> = listOf(firstPane, secondPane)

    override val content: @Composable (() -> Unit) = {

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(weights.twoPanesScenePaneWeight)) {
                firstPane.Content()
            }
            Column(modifier = Modifier.weight(1 - weights.twoPanesScenePaneWeight)) {
                secondPane.Content()
            }
        }
    }
}


internal class BottomPaneScene<T : Any>(
    val pane: NavEntry<T>,
    val properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
    override val previousEntries: List<NavEntry<T>>,
    override val key: Any,
    val onBack: (Int) -> Unit
) : Scene<T> {

    override val entries: List<NavEntry<T>> = listOf(pane)

    @OptIn(ExperimentalMaterial3Api::class)
    override val content: @Composable (() -> Unit) = {

        ModalBottomSheet(
            onDismissRequest = { onBack(1) },
            properties = properties
        ) {
            pane.Content()
        }

    }
}

class ListDetailNoPlaceholderSceneStrategy<T : Any>(val sceneDefaults: SceneDefaults = SceneDefaults()) :
    SceneStrategy<T> {

    companion object {
        internal const val MAIN = "main"
        internal const val DETAIL = "detail"
        internal const val SUPPORT = "support"
        internal const val THIRD_PANEL = "thirdPanel"

        @JvmStatic
        fun main() = mapOf(MAIN to true)

        @JvmStatic
        fun detail() = mapOf(DETAIL to true)

        @JvmStatic
        fun thirdPanel() = mapOf(THIRD_PANEL to true)

        @JvmStatic
        fun support() = mapOf(SUPPORT to true)
    }

    data class SceneDefaults(
        val twoPanesScenePaneWeight: Float = .5f,
        val threePanesSceneFirstPaneWeight: Float = .4f,
        val threePanesSceneSecondPaneWeight: Float = .3f,
        val threePanesSceneThirdPaneWeight: Float = .3f,
        val bottomSheetProperties: ModalBottomSheetProperties = ModalBottomSheetProperties()
    )

    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>, onBack: (Int) -> Unit
    ): Scene<T>? {

        val windowSizeClass =
            currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass
        val isLastEntrySupportingPane = entries.lastOrNull()?.metadata[SUPPORT] == true

        // Condition 1: Only return a Scene if the window is sufficiently wide to render two panes,
        // or if a supporting pane is detected.
        //
        // We use isWidthAtLeastBreakpoint with WIDTH_DP_MEDIUM_LOWER_BOUND (600dp).
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return if (isLastEntrySupportingPane) {
                buildSupportingPaneScene(
                    pane = entries.last(),
                    previousEntry = entries[entries.size - 2],
                    onBack = onBack
                )
            } else {
                null
            }
        }

        if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND) && entries.size >= 3) {
            return buildAdaptiveThreePanesScene(entries)
        }

        if (entries.size >= 2) {
            return buildAdaptiveTwoPanesScene(entries)
        }
        return null
    }

    private fun buildAdaptiveThreePanesScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.last()
        val secondLastEntry = entries[entries.size - 2]
        val thirdLastEntry = entries[entries.size - 3]

        return if (lastEntry.metadata[THIRD_PANEL] == true && secondLastEntry.metadata[DETAIL] == true && thirdLastEntry.metadata[MAIN] == true) {
            AdaptiveThreePaneScene(
                firstPane = thirdLastEntry,
                secondPane = secondLastEntry,
                thirdPane = lastEntry,
                weights = sceneDefaults,
                previousEntries = listOf(thirdLastEntry, secondLastEntry),
                key = Triple(
                    thirdLastEntry.contentKey, secondLastEntry.contentKey, lastEntry.contentKey
                )
            )
        } else {
            null
        }
    }

    private fun NavEntry<T>.isMainPane() : Boolean = metadata[MAIN] == true
    private fun NavEntry<T>.isSecondPane() : Boolean = metadata[DETAIL] == true || metadata[SUPPORT] == true
    private fun NavEntry<T>.isLastPane() : Boolean = metadata[THIRD_PANEL] == true

    private fun buildAdaptiveTwoPanesScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.last()
        val secondLastEntry = entries[entries.size - 2]

        return if (lastEntry.isSecondPane() && secondLastEntry.isMainPane()) {
            buildListDetailScene(secondLastEntry, lastEntry)
        } else if (lastEntry.isLastPane() && secondLastEntry.isSecondPane() && entries.size >= 3) {
            val zeroethEntry = entries[entries.size - 3]
            buildDetailAndThirdPanelScene(secondLastEntry, lastEntry, zeroethEntry)
        } else {
            null
        }
    }

    private fun buildListDetailScene(firstEntry: NavEntry<T>, secondEntry: NavEntry<T>): Scene<T> {
        return AdaptiveTwoPaneScene(
            firstPane = firstEntry,
            secondPane = secondEntry,
            weights = sceneDefaults,
            previousEntries = listOf(firstEntry),
            key = Pair(firstEntry.contentKey, secondEntry.contentKey)
        )
    }

    private fun buildDetailAndThirdPanelScene(
        firstEntry: NavEntry<T>, secondEntry: NavEntry<T>, previousEntry: NavEntry<T>
    ): Scene<T> {
        return AdaptiveTwoPaneScene(
            firstPane = firstEntry,
            secondPane = secondEntry,
            weights = sceneDefaults,
            previousEntries = listOf(previousEntry, firstEntry),
            key = Pair(firstEntry.contentKey, secondEntry.contentKey)
        )
    }

    private fun buildSupportingPaneScene(
        pane: NavEntry<T>,
        previousEntry: NavEntry<T>,
        onBack: (Int) -> Unit
    ): Scene<T> {
        return BottomPaneScene(
            pane = pane,
            properties = sceneDefaults.bottomSheetProperties,
            previousEntries = listOf(previousEntry),
            key = pane.contentKey,
            onBack = onBack
        )
    }
}
