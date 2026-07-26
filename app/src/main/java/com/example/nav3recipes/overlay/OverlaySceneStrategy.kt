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
 * distributed under the License is an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nav3recipes.overlay

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * An [OverlayScene] that renders an [overlayEntry] on top of an [overlaidEntry] within a [Box].
 *
 * This scene enables shared element transitions between the overlaid and overlaying entries,
 * which is not currently supported by Nav3's screen-to-window transitions.
 *
 * @property key The scene key.
 * @property previousEntries The entries that were on the back stack before this scene.
 * @property overlaidEntry The [NavEntry] displayed underneath.
 * @property overlayEntry The [NavEntry] displayed on top.
 * @property overlayHeightFraction The fraction of the screen height the overlay occupies (0.0–1.0).
 * @property onBack The callback invoked when the user dismisses the overlay.
 */
data class OverlayScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val overlaidEntry: NavEntry<T>,
    val overlayEntry: NavEntry<T>,
    val overlayHeightFraction: Float = 0.75f,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(overlaidEntry, overlayEntry)

    override val content: @Composable (() -> Unit) = {
        Box(Modifier.fillMaxSize()) {
            // The underlying content
            overlaidEntry.Content()

            // The overlay content, positioned at the bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height((overlayHeightFraction * 100).dp),
                contentAlignment = Alignment.Center
            ) {
                overlayEntry.Content()
            }
        }

        // Back handler to dismiss the overlay
        BackHandler {
            onBack()
        }
    }
}

/**
 * A [SceneStrategy] that creates an [OverlayScene] when the last entry in the back stack
 * has been marked with [overlay].
 *
 * The strategy takes the second-to-last entry as the overlaid content and the last entry
 * (with the overlay metadata) as the overlay content.
 *
 * This strategy should be added before any non-overlay scene strategies.
 *
 * @property overlayHeightFraction The fraction of the screen height the overlay occupies.
 */
class OverlaySceneStrategy<T : Any>(
    private val overlayHeightFraction: Float = 0.75f,
) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (entries.size < 2) return null

        val lastEntry = entries.lastOrNull() ?: return null
        val overlayConfig = lastEntry.metadata[OverlayKey] ?: return null

        val overlaidEntry = entries[entries.size - 2]

        return OverlayScene(
            key = lastEntry.contentKey,
            previousEntries = entries.dropLast(1),
            overlaidEntry = overlaidEntry,
            overlayEntry = lastEntry,
            overlayHeightFraction = overlayConfig.overlayHeightFraction ?: overlayHeightFraction,
            onBack = onBack,
        )
    }

    companion object {
        /**
         * Function to be called on the [NavEntry.metadata] to mark this entry as something that
         * should be displayed as an overlay on top of the previous entry.
         *
         * @param overlayHeightFraction Optional fraction of screen height (0.0–1.0) for the overlay.
         *   Defaults to 0.75 if not specified.
         */
        fun overlay(overlayHeightFraction: Float? = null) = metadata {
            put(OverlayKey, OverlayConfig(overlayHeightFraction))
        }

        /**
         * Metadata key for overlay configuration.
         */
        object OverlayKey : NavMetadataKey<OverlayConfig>
    }
}

/**
 * Configuration for an overlay entry.
 *
 * @property overlayHeightFraction The fraction of screen height the overlay occupies.
 */
data class OverlayConfig(
    val overlayHeightFraction: Float? = null,
)
