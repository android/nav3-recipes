package com.example.nav3recipes.navscenedecorator

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun Modifier.cacheSize(useCachedSize: Boolean): Modifier {
    var lastSize by remember { mutableStateOf<IntSize?>(null) }

    return this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        if (!useCachedSize) {
            lastSize = IntSize(placeable.width, placeable.height)
        }

        val width = if (useCachedSize) lastSize?.width ?: placeable.width else placeable.width
        val height = if (useCachedSize) lastSize?.height ?: placeable.height else placeable.height

        layout(width, height) {
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