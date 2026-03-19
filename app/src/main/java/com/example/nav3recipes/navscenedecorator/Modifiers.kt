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