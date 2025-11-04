package com.example.nav3recipes.scenes.twopane

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.toSize
import androidx.navigation3.runtime.NavEntry
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SlideToPopLayout(
    isCurrentScene: Boolean,
    entries: List<NavEntry<*>>,
    onBack: () -> Unit,
) {
    val density = LocalDensity.current

    val splitLayoutState = remember {
        SplitLayoutState(
            orientation = Orientation.Horizontal,
            maxCount = 2,
            minSize = 1.dp,
            visibleCount = {
                entries.size
            },
            keyAtIndex = { index ->
                entries[index].contentKey
            }
        )
    }
    val draggableState = rememberDraggableState { delta ->
        splitLayoutState.dragBy(
            index = 0,
            delta = with(density) { delta.toDp() }
        )
    }
    SplitLayout(
        state = splitLayoutState,
        modifier = Modifier.fillMaxSize(),
        itemSeparators = { _, offset ->
            PaneSeparator(
                splitLayoutState = splitLayoutState,
                draggableState = draggableState,
                offset = offset,
                onBack = onBack,
            )
        },
        itemContent = { index ->
            Box(
                modifier = Modifier.constrainedSizePlacement(
                    orientation = Orientation.Horizontal,
                    minSize = splitLayoutState.size / 3,
                    atStart = index == 0,
                )
            ) {
                entries[index].Content()
            }
        },
    )

    if (isCurrentScene) {
        SlideToPopNavigationEventHandler(
            enabled = (entries.lastOrNull()?.metadata?.containsKey(TwoPaneScene.TWO_PANE_KEY) == true),
            splitLayoutState = splitLayoutState,
            draggableState = draggableState,
            onBack = onBack,
        )
    }
}

@Composable
private fun PaneSeparator(
    splitLayoutState: SplitLayoutState,
    draggableState: DraggableState,
    modifier: Modifier = Modifier,
    offset: Dp,
    onBack: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val active = isHovered || isPressed || isDragged
    val width by animateDpAsState(
        label = "App Pane Draggable thumb",
        targetValue =
            if (active) DraggableDividerSizeDp
            else 8.dp
    )
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = with(density) { (offset - (width / 2)).roundToPx() },
                    y = 0,
                )
            }
            .width(width)
            .fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(DraggableDividerSizeDp)
                .draggable(
                    state = draggableState,
                    orientation = splitLayoutState.orientation,
                    interactionSource = interactionSource,
                    onDragStopped = {
                        if (splitLayoutState.weightAt(0) > DragPopThreshold) onBack()
                    }
                )
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .hoverable(interactionSource)
        ) {
            if (active) Icon(
                modifier = Modifier
                    .align(Alignment.Center),
                imageVector = Icons.Rounded.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

private val SplitLayoutState.firstPaneWidth
    get() = (weightAt(0) * size.value).roundToInt()

@Composable
private fun SlideToPopNavigationEventHandler(
    enabled: Boolean,
    splitLayoutState: SplitLayoutState,
    draggableState: DraggableState,
    onBack: () -> Unit,
) {
    val currentlyEnabled by rememberUpdatedState(enabled)
    var started by remember { mutableStateOf(false) }
    var widthAtStart by remember { mutableIntStateOf(0) }
    var desiredPaneWidth by remember { mutableFloatStateOf(0f) }

    NavigationBackHandler(
        state = rememberNavigationEventState(
            currentInfo = SecondaryPaneCloseNavigationEventInfo,
        ),
        isBackEnabled = currentlyEnabled,
        onBackCancelled = {
            started = false
        },
        onBackCompleted = {
            started = false
            onBack()
        },
    )

    val navigationEventDispatcher = LocalNavigationEventDispatcherOwner.current!!
        .navigationEventDispatcher

    LaunchedEffect(navigationEventDispatcher) {
        var wasIdle = true
        navigationEventDispatcher
            .transitionState
            .collect { state ->
                when (state) {
                    NavigationEventTransitionState.Idle -> wasIdle = true
                    is NavigationEventTransitionState.InProgress -> if (currentlyEnabled) {
                        if (wasIdle) {
                            widthAtStart = splitLayoutState.firstPaneWidth
                            started = true
                        }
                        val progress = state.latestEvent.progress
                        val distanceToCover = splitLayoutState.size.value - widthAtStart
                        desiredPaneWidth = (progress * distanceToCover) + widthAtStart
                        wasIdle = false
                    }
                }
            }
    }

    // Make sure desiredPaneWidth is synced with paneSplitState.width before the back gesture
    LaunchedEffect(Unit) {
        snapshotFlow { started to splitLayoutState.firstPaneWidth }
            .collect { (isStarted, firstPaneWidth) ->
                if (isStarted) return@collect
                desiredPaneWidth = firstPaneWidth.toFloat()
            }
    }

    // Dispatch changes as the user presses back
    LaunchedEffect(Unit) {
        snapshotFlow { started to desiredPaneWidth }
            .collect { (isStarted, targetWidth) ->
                if (!isStarted) return@collect
                draggableState.dispatchRawDelta(
                    delta = targetWidth - splitLayoutState.firstPaneWidth.toFloat()
                )
            }
    }
}

/**
 * State describing the behavior for [SplitLayout].
 *
 * @param orientation The orientation of the layout.
 * @param maxCount The maximum number of children in the layout.
 * @param minSize The minimum size of a child in the layout.
 * @param visibleCount The number of children that are currently visible in the layout. This
 * lambda can be snapshot aware.
 * @param keyAtIndex Provides a key for the item at an index to identify its position in the
 * case of visibility changes of other indices. Defaults to the index of the item.
 */
@Stable
class SplitLayoutState(
    val orientation: Orientation,
    val maxCount: Int,
    minSize: Dp = 80.dp,
    internal val visibleCount: () -> Int = { maxCount },
    internal val keyAtIndex: SplitLayoutState.(Int) -> Any = { it },
) {

    private val weightMap = mutableStateMapOf<Int, Float>().apply {
        (0..<maxCount).forEach { index -> put(index, 1f / maxCount) }
    }

    /**
     * Th sum of the weights of the visible children in the layout
     */
    private val weightSum by derivedStateOf {
        checkVisibleCount()
        (0..<visibleCount()).sumOf { weightMap.getValue(it).toDouble() }.toFloat()
    }

    private var minSize by mutableStateOf(minSize)

    var size by mutableStateOf(0.dp)
        internal set

    init {
        checkVisibleCount()
    }

    /**
     * Returns the weight of the child at the specified index.
     * @param index The index whose weight should be returned.
     */
    fun weightAt(index: Int): Float = weightMap.getValue(index) / weightSum

    /**
     * Attempts to set the weight at [index] and returns the status of the attempt. Reasons
     * for failure include:
     * - Negative weights
     * - Weights that would violate [minSize].
     * - Weights that are greater than the weight sum.
     *
     * @param index The index to set the weight at.
     * @param weight The weight of this index relative to the [weightSum] of the layout.
     */
    private fun setWeightAt(index: Int, weight: Float): Boolean {
        if (weight <= 0f || weight > weightSum) return false
        if (weight * size < minSize) return false

        val oldWeight = weightMap.getValue(index)
        val weightDifference = oldWeight - weight

        var adjustedIndex = -1
        for (i in 0..<maxCount) {
            val searchIndex = abs(index + i) % maxCount
            if (searchIndex == index) continue

            val adjustedWidth = (weightMap.getValue(searchIndex) + weightDifference) * size
            if (adjustedWidth < minSize) continue

            adjustedIndex = searchIndex
            break
        }
        if (adjustedIndex < 0) return false

        weightMap[index] = weight
        weightMap[adjustedIndex] = weightMap.getValue(adjustedIndex) + weightDifference
        return true
    }

    /**
     * Attempts to resize the child at the specified index by the specified delta and returns the
     * status of the attempt.
     *
     * @param index The index to drag.
     * @param delta The amount to resize [index] by.
     */
    fun dragBy(index: Int, delta: Dp): Boolean {
        val oldWeight = weightAt(index)
        val currentSize = oldWeight * size
        val newSize = currentSize + delta
        val newWeight = (newSize / size) * weightSum
        return setWeightAt(
            index = index,
            weight = newWeight
        )
    }

    private fun offsetAt(index: Int): Dp {
        var offset = 0.dp
        var start = -1
        while (++start <= index) {
            offset += weightAt(start) * size
        }
        return offset
    }

    private fun checkVisibleCount() {
        check(visibleCount() <= maxCount) {
            "initialVisibleCount must be less than or equal to maxCount."
        }
    }

    internal companion object {

        @Composable
        fun SplitLayoutState.Separators(
            separator: @Composable (paneIndex: Int, offset: Dp) -> Unit
        ) {
            val visibleCount = visibleCount()
            if (visibleCount > 1)
                for (index in 0..<visibleCount)
                    if (index != visibleCount - 1)
                        separator(index, offsetAt(index))
        }

        fun SplitLayoutState.updateSize(size: IntSize, density: Density) {
            this.size = with(density) {
                val dpSize = size.toSize().toDpSize()
                when (orientation) {
                    Orientation.Vertical -> dpSize.height
                    Orientation.Horizontal -> dpSize.width
                }
            }
        }
    }
}

/**
 * A layout for consecutively placing resizable children along the axis of
 * [SplitLayoutState.orientation]. The children should be the same size perpendicular to
 * [SplitLayoutState.orientation].
 *
 * Children may be hidden by writing to [SplitLayoutState.visibleCount].
 *
 * @param state The state of the layout.
 * @param modifier The modifier to be applied to the layout.
 * @param itemSeparators Separators to be drawn when more than one child is visible.
 * @param itemContent the content to be drawn in each visible index.
 */
@Composable
fun SplitLayout(
    state: SplitLayoutState,
    modifier: Modifier = Modifier,
    itemSeparators: @Composable (paneIndex: Int, offset: Dp) -> Unit = { _, _ -> },
    itemContent: @Composable (Int) -> Unit,
) = with(SplitLayoutState) {
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .onSizeChanged {
                state.updateSize(it, density)
            },
    ) {
        val visibleCount = state.visibleCount()
        when (state.orientation) {
            Orientation.Vertical -> Column(
                modifier = Modifier
                    .matchParentSize(),
            ) {
                for (index in 0..<visibleCount) {
                    key(state.keyAtIndex(state, index)) {
                        Box(
                            modifier = Modifier
                                .weight(state.weightAt(index))
                        ) {
                            itemContent(index)
                        }
                    }
                }
            }

            Orientation.Horizontal -> Row(
                modifier = Modifier
                    .matchParentSize(),
            ) {
                for (index in 0..<visibleCount) {
                    key(state.keyAtIndex(state, index)) {
                        Box(
                            modifier = Modifier
                                .weight(state.weightAt(index))
                        ) {
                            itemContent(index)
                        }
                    }
                }
            }
        }
        state.Separators(itemSeparators)
    }
}

fun Modifier.constrainedSizePlacement(
    orientation: Orientation,
    minSize: Dp,
    atStart: Boolean,
) = layout { measurable, constraints ->
    val minPaneSize = minSize.roundToPx()
    val actualConstraints = when (orientation) {
        Orientation.Horizontal ->
            if (!constraints.hasBoundedWidth || constraints.maxWidth >= minPaneSize) constraints
            else constraints.copy(maxWidth = minPaneSize, minWidth = minPaneSize)

        Orientation.Vertical ->
            if (!constraints.hasBoundedHeight || constraints.maxHeight >= minPaneSize) constraints
            else constraints.copy(maxHeight = minPaneSize, minHeight = minPaneSize)
    }
    val placeable = measurable.measure(actualConstraints)

    layout(width = placeable.width, height = placeable.height) {
        // In the below, when the dimension is larger than the constraints, the placement is
        // coordinate is halved because when the dimension is larger than the constraints, the
        // content is automatically centered within the constraints.
        when (orientation) {
            Orientation.Horizontal -> placeable.placeRelativeWithLayer(
                x = if (constraints.maxWidth >= minPaneSize) 0
                else when {
                    atStart -> constraints.maxWidth - minPaneSize
                    else -> minPaneSize - constraints.maxWidth
                } / 2,
                y = 0,
            )

            Orientation.Vertical -> placeable.placeRelativeWithLayer(
                x = 0,
                y = if (constraints.maxHeight >= minPaneSize) 0
                else when {
                    atStart -> constraints.maxHeight - minPaneSize
                    else -> minPaneSize - constraints.maxHeight
                } / 2,
            )
        }
    }
}

internal object SecondaryPaneCloseNavigationEventInfo : NavigationEventInfo()

private val DraggableDividerSizeDp = 64.dp

private const val DragPopThreshold = 0.7f
