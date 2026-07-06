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

package com.example.nav3recipes.scenes.columnar

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

/**
 * A [Scene] that displays an arbitrary number of directory [NavEntry] columns side-by-side
 * and an optional detail [NavEntry] column at the right end.
 */
class ColumnarScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val directoryEntries: List<NavEntry<T>>,
    val detailEntry: NavEntry<T>?,
    val onBack: () -> Unit
) : Scene<T> {
    override val entries: List<NavEntry<T>> =
        if (detailEntry != null) directoryEntries + detailEntry else directoryEntries

    override val content: @Composable () -> Unit = {
        val listState = rememberLazyListState()

        // When the number of entries changes, scroll to the last item
        LaunchedEffect(entries.size) {
            if (entries.isNotEmpty()) {
                listState.scrollToItem(
                    index = entries.size - 1,
                )
            }
        }

        var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
        var isPredictiveBackInProgress by remember { mutableStateOf(false) }
        var poppedEntryKey by remember { mutableStateOf<Any?>(null) }

        val lastEntry = entries.lastOrNull()

        PredictiveBackHandler(enabled = entries.size > 1) { progressFlow ->
            isPredictiveBackInProgress = true
            try {
                progressFlow.collect { backEvent ->
                    predictiveBackProgress = backEvent.progress
                }
                // Successfully completed the gesture. Track the entry being removed to prevent flash/tearing.
                poppedEntryKey = lastEntry?.contentKey
                onBack()
            } finally {
                isPredictiveBackInProgress = false
                predictiveBackProgress = 0f
            }
        }

        // Reset the popped key once the entry is officially removed from the backstack entries
        LaunchedEffect(entries) {
            if (poppedEntryKey != null && entries.none { it.contentKey == poppedEntryKey }) {
                poppedEntryKey = null
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(entries, key = { it.contentKey }) { entry ->
                ColumnWrapper(
                    entry = entry,
                    width = if (entry == detailEntry) 360.dp else 240.dp,
                    isLastEntry = entry == lastEntry,
                    poppedEntryKey = poppedEntryKey,
                    isPredictiveBackInProgress = isPredictiveBackInProgress,
                    predictiveBackProgressProvider = { predictiveBackProgress }
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColumnarScene<*>) return false

        if (key != other.key) return false
        if (previousEntries != other.previousEntries) return false
        if (directoryEntries != other.directoryEntries) return false
        if (detailEntry != other.detailEntry) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + previousEntries.hashCode()
        result = 31 * result + directoryEntries.hashCode()
        result = 31 * result + (detailEntry?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun directoryPane() = metadata {
            put(DirectoryKey, true)
        }

        fun detailPane() = metadata {
            put(DetailKey, true)
        }
    }

    object DirectoryKey : NavMetadataKey<Boolean>
    object DetailKey : NavMetadataKey<Boolean>
}

@Composable
private fun LazyItemScope.ColumnWrapper(
    entry: NavEntry<*>,
    width: Dp,
    isLastEntry: Boolean,
    poppedEntryKey: Any?,
    isPredictiveBackInProgress: Boolean,
    predictiveBackProgressProvider: () -> Float,
) {
    val isPoppingThisEntry = entry.contentKey == poppedEntryKey
    val zIndex = if (isPoppingThisEntry || (isLastEntry && isPredictiveBackInProgress)) -1f else 0f

    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .zIndex(zIndex)
            .animateItem(placementSpec = null)
            .graphicsLayer {
                if (isPoppingThisEntry || (isLastEntry && isPredictiveBackInProgress)) {
                    val progress = if (isPoppingThisEntry) 1f else predictiveBackProgressProvider()
                    alpha = 1f - progress
                    translationX = -progress * size.width * 0.3f
                }
            }
    ) {
        entry.Content()
    }
}

@Composable
fun <T : Any> rememberColumnarSceneStrategy(): ColumnarSceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    return remember(windowSizeClass) { ColumnarSceneStrategy(windowSizeClass) }
}

class ColumnarSceneStrategy<T : Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }

        val detailEntry =
            entries.lastOrNull()?.takeIf { it.metadata.contains(ColumnarScene.DetailKey) }

        // Go backwards through the entries (excluding the top detail entry if present)
        val entriesToCheck = if (detailEntry != null) entries.dropLast(1) else entries
        val directories =
            entriesToCheck.takeLastWhile { it.metadata.contains(ColumnarScene.DirectoryKey) }

        if (directories.isEmpty()) return null

        return ColumnarScene(
            // Keep the key fixed so the ColumnarScene itself is responsible for showing and hiding entries
            key = directories.first().contentKey,
            // Keeping all but the most recent scene
            previousEntries = entries.dropLast(1),
            directoryEntries = directories,
            detailEntry = detailEntry,
            onBack = onBack
        )
    }
}
