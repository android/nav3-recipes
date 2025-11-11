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
package com.example.nav3recipes.scenes.xr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.platform.LocalSpatialConfiguration
import androidx.xr.compose.spatial.ApplicationSubspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.fillMaxHeight
import androidx.xr.compose.subspace.layout.fillMaxWidth
import com.example.nav3recipes.scenes.xr.XrNavigationKeys.FIRST_PANE_KEY

class XrSinglePaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val firstEntry: NavEntry<T>
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(firstEntry)
    override val content: @Composable (() -> Unit) = {
        ApplicationSubspace {
            SpatialPanel(
                modifier = SubspaceModifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                firstEntry.Content()
            }

        }
    }
}

@Composable
fun <T : Any> rememberXrSinglePaneStrategy(): XrSinglePaneSceneStrategy<T> {
    val isSpatialUiEnabled = LocalSpatialCapabilities.current.isSpatialUiEnabled

    return remember(isSpatialUiEnabled) {
        XrSinglePaneSceneStrategy(
            isSpatialUiEnabled = isSpatialUiEnabled
        )
    }
}

class XrSinglePaneSceneStrategy<T : Any>(val isSpatialUiEnabled: Boolean) :
    SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!isSpatialUiEnabled) return null

        val firstPane =
            entries.findLast { it.metadata.containsKey(FIRST_PANE_KEY) } ?: return null

        // We use the list's contentKey to uniquely identify the scene.
        // This allows the detail panes to be animated in and out by the scene, rather than
        // having NavDisplay animate the whole scene out when the selected detail item changes.
        val sceneKey = firstPane.contentKey

        return XrSinglePaneScene(
            key = sceneKey,
            previousEntries = entries.dropLast(1),
            firstPane
        )
    }

}