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

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.platform.LocalSpatialConfiguration
import androidx.xr.compose.spatial.ApplicationSubspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.fillMaxHeight
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.scenecore.scene
import com.example.nav3recipes.scenes.xr.XrNavigationKeys.FIRST_PANE_KEY
import com.example.nav3recipes.scenes.xr.XrNavigationKeys.SECOND_PANE_KEY

private const val METERS_TO_DP = 1000f
class XrTwoPaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val firstEntry: NavEntry<T>,
    val secondEntry: NavEntry<T>,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(firstEntry, secondEntry)
    override val content: @Composable (() -> Unit) = {
        ApplicationSubspace {
            SpatialRow(SubspaceModifier.fillMaxWidth()) {
                SpatialPanel(
                    modifier = SubspaceModifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    firstEntry.Content()
                }
                SpatialPanel(
                    modifier = SubspaceModifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    secondEntry.Content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun <T : Any> rememberXrTwoPaneStrategy(): XrTwoPaneSceneStrategy<T> {
    val isSpatialUiEnabled = LocalSpatialCapabilities.current.isSpatialUiEnabled

    val wsc = WindowSizeClass.calculateFromSize(size = fetchXrWindowBounds())

    return remember(isSpatialUiEnabled, wsc) {
        XrTwoPaneSceneStrategy(
            isSpatialUiEnabled = isSpatialUiEnabled,
            wsc = wsc
        )
    }
}

@Composable
private fun fetchXrWindowBounds(): DpSize {
    val xrSession = LocalSession.current

    if (xrSession == null) {
        val xrBounds = LocalSpatialConfiguration.current.bounds
        return DpSize(xrBounds.width, xrBounds.height)
    }

    val boundingBox = xrSession.scene.activitySpace.recommendedContentBoxInFullSpace
    val xrWidth = (boundingBox.max.x - boundingBox.min.x) * METERS_TO_DP
    val xrHeight = (boundingBox.max.y - boundingBox.min.y) * METERS_TO_DP

    return DpSize(width = xrWidth.dp, height = xrHeight.dp)
}

class XrTwoPaneSceneStrategy<T : Any>(val isSpatialUiEnabled: Boolean, val wsc: WindowSizeClass) :
    SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!isSpatialUiEnabled || wsc.widthSizeClass == WindowWidthSizeClass.Compact) return null

        val secondPane =
            entries.lastOrNull()?.takeIf { it.metadata.containsKey(SECOND_PANE_KEY) }
                ?: return null
        val firstPane =
            entries.findLast { it.metadata.containsKey(FIRST_PANE_KEY) } ?: return null

        // We use the list's contentKey to uniquely identify the scene.
        // This allows the detail panes to be animated in and out by the scene, rather than
        // having NavDisplay animate the whole scene out when the selected detail item changes.
        val sceneKey = firstPane.contentKey

        return XrTwoPaneScene(
            key = sceneKey,
            previousEntries = entries.dropLast(1),
            firstPane,
            secondPane
        )
    }

}