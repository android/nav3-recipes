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

package com.example.nav3recipes.navscenedecorator

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

/**
 * A modifier that caches the measured size of the component and optionally reuses it. For example,
 * this can be used to maintain the size of an element that contains moveable content.
 *
 * @param useCachedSize If true, the modifier will use the previously measured and cached size
 * (if available) instead of the current size. If false, it measures normally and caches the new size.
 */
fun Modifier.cacheSize(useCachedSize: Boolean): Modifier =
    this.then(CacheSizeElement(useCachedSize))

private data class CacheSizeElement(
    val useCachedSize: Boolean
) : ModifierNodeElement<CacheSizeNode>() {

    override fun create() = CacheSizeNode(useCachedSize)

    override fun update(node: CacheSizeNode) {
        node.useCachedSize = useCachedSize
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cacheSize"
        properties["useCachedSize"] = useCachedSize
    }
}

private class CacheSizeNode(
    useCachedSize: Boolean
) : Modifier.Node(), LayoutModifierNode {

    var useCachedSize: Boolean = useCachedSize
        set(value) {
            if (field != value) {
                field = value
                invalidateMeasurement()
            }
        }

    private var isSizeCached = false
    private var cachedSize: IntSize = IntSize.Zero

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        val currentSize = IntSize(placeable.width, placeable.height)

        val size = if (useCachedSize && isSizeCached) {
            cachedSize
        } else {
            currentSize
        }

        cachedSize = size
        isSizeCached = true

        return layout(size.width, size.height) {
            placeable.placeRelative(0, 0)
        }
    }
}

/**
 * A modifier that calculates how much the component overlaps with the given [WindowInsets]
 * and reports the overlap as [PaddingValues].
 *
 * This can be used to dynamically adjust the layout of content that is partially obscured
 * by system bars or other window insets, such as by using it as the `contentPadding` parameter
 * for a [androidx.compose.foundation.lazy.LazyColumn] or [androidx.compose.foundation.lazy.LazyRow]
 *
 * @param insets The [WindowInsets] to calculate the overlap against.
 * @param onOverlapChanged A callback invoked whenever the overlap changes, providing the overlap as [PaddingValues].
 */
@Composable
fun Modifier.onWindowInsetsOverlapChanged(
    insets: WindowInsets,
    onOverlapChanged: (PaddingValues) -> Unit
): Modifier {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val layoutDirection = LocalLayoutDirection.current

    return this.then(
        WindowInsetsOverlapElement(
            insets = insets,
            density = density,
            windowHeight = windowInfo.containerSize.height.toFloat(),
            windowWidth = windowInfo.containerSize.width.toFloat(),
            layoutDirection = layoutDirection,
            onOverlapChanged = onOverlapChanged
        )
    )
}

private data class WindowInsetsOverlapElement(
    val insets: WindowInsets,
    val density: Density,
    val windowHeight: Float,
    val windowWidth: Float,
    val layoutDirection: LayoutDirection,
    val onOverlapChanged: (PaddingValues) -> Unit
) : ModifierNodeElement<WindowInsetsOverlapNode>() {
    override fun create() = WindowInsetsOverlapNode(
        insets, density, windowHeight, windowWidth, layoutDirection, onOverlapChanged
    )

    override fun update(node: WindowInsetsOverlapNode) {
        node.insets = insets
        node.density = density
        node.windowHeight = windowHeight
        node.windowWidth = windowWidth
        node.layoutDirection = layoutDirection
        node.onOverlapChanged = onOverlapChanged

        // Recalculate padding when modifier properties (like insets) change,
        // even if the component hasn't moved or changed size (no layout pass).
        node.calculatePadding()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "onWindowInsetsOverlapChanged"
        properties["insets"] = insets
    }
}

private class WindowInsetsOverlapNode(
    var insets: WindowInsets,
    var density: Density,
    var windowHeight: Float,
    var windowWidth: Float,
    var layoutDirection: LayoutDirection,
    var onOverlapChanged: (PaddingValues) -> Unit
) : Modifier.Node(), GlobalPositionAwareModifierNode {

    // Cache the layout coordinates so padding can be recalculated
    // when insets change without triggering a new global positioning pass.
    private var lastCoordinates: LayoutCoordinates? = null

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        lastCoordinates = coordinates
        calculatePadding()
    }

    fun calculatePadding() {
        val coordinates = lastCoordinates ?: return
        val screenRect = coordinates.boundsInWindow()

        val topOverlap = (insets.getTop(density) - screenRect.top).coerceAtLeast(0f)
        val bottomOverlap = (screenRect.bottom - (windowHeight - insets.getBottom(density))).coerceAtLeast(0f)
        val leftOverlap = (insets.getLeft(density, layoutDirection) - screenRect.left).coerceAtLeast(0f)
        val rightOverlap = (screenRect.right - (windowWidth - insets.getRight(density, layoutDirection))).coerceAtLeast(0f)

        with(density) {
            onOverlapChanged(
                PaddingValues.Absolute(
                    left = leftOverlap.toDp(),
                    top = topOverlap.toDp(),
                    right = rightOverlap.toDp(),
                    bottom = bottomOverlap.toDp()
                )
            )
        }
    }
}
