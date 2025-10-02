package com.example.nav3recipes.scenes.listdetailnoplaceholder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.Scene
import androidx.navigation3.ui.SceneStrategy
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

fun columnsByComposableWidth(width: Dp): Int {
    return when {
        width >= WIDTH_DP_EXTRA_LARGE_LOWER_BOUND.dp -> 5
        width >= WIDTH_DP_LARGE_LOWER_BOUND.dp -> 4
        width >= WIDTH_DP_EXPANDED_LOWER_BOUND.dp -> 3
        width >= WIDTH_DP_MEDIUM_LOWER_BOUND.dp -> 2
        else -> 1
    }
}

class AdaptiveThreePaneScene<T : Any>(
    val firstPane: NavEntry<T>,
    val secondPane: NavEntry<T>,
    val thirdPane: NavEntry<T>,
    val weights: ListDetailNoPlaceholderSceneStrategy.SceneWeightsDefaults,
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

class AdaptiveTwoPaneScene<T : Any>(
    val firstPane: NavEntry<T>,
    val secondPane: NavEntry<T>,
    val weights: ListDetailNoPlaceholderSceneStrategy.SceneWeightsDefaults,
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

class AdaptiveSinglePaneScene<T : Any>(
    val pane: NavEntry<T>, override val previousEntries: List<NavEntry<T>>, override val key: Any
) : Scene<T> {

    override val entries: List<NavEntry<T>> = listOf(pane)

    override val content: @Composable (() -> Unit) = {
        pane.Content()
    }
}

class ListDetailNoPlaceholderSceneStrategy<T : Any>(val sceneWeights: SceneWeightsDefaults = SceneWeightsDefaults()) :
    SceneStrategy<T> {

    companion object {
        internal const val LIST = "list"
        internal const val DETAIL = "detail"
        internal const val THIRD_PANEL = "thirdPanel"

        fun list() = mapOf(LIST to true)
        fun detail() = mapOf(DETAIL to true)

        fun thirdPanel() = mapOf(THIRD_PANEL to true)
    }

    data class SceneWeightsDefaults(
        val twoPanesScenePaneWeight: Float = .5f,
        val threePanesSceneFirstPaneWeight: Float = .4f,
        val threePanesSceneSecondPaneWeight: Float = .3f,
        val threePanesSceneThirdPaneWeight: Float = .3f,
    )

    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>, onBack: (Int) -> Unit
    ): Scene<T>? {

        val windowSizeClass =
            currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass

        // Condition 1: Only return a Scene if the window is sufficiently wide to render two panes.
        // We use isWidthAtLeastBreakpoint with WIDTH_DP_MEDIUM_LOWER_BOUND (600dp).
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }

        if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND) && entries.size >= 3) {
            return buildAdaptiveThreePanesScene(entries)
        }

        if (entries.size >= 2) {
            return buildAdaptiveTwoPanesScene(entries)
        }
        //Only the list is available
        if (entries.isNotEmpty()) {
            return buildAdaptiveListScene(entries)
        }
        return null
    }

    private fun buildAdaptiveThreePanesScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.last()
        val secondLastEntry = entries[entries.size - 2]
        val thirdLastEntry = entries[entries.size - 3]

        return if (lastEntry.metadata[THIRD_PANEL] == true && secondLastEntry.metadata[DETAIL] == true && thirdLastEntry.metadata[LIST] == true) {
            AdaptiveThreePaneScene(
                firstPane = thirdLastEntry,
                secondPane = secondLastEntry,
                thirdPane = lastEntry,
                weights = sceneWeights,
                previousEntries = listOf(thirdLastEntry, secondLastEntry),
                key = Triple(
                    thirdLastEntry.contentKey, secondLastEntry.contentKey, lastEntry.contentKey
                )
            )
        } else {
            null
        }
    }

    private fun buildAdaptiveTwoPanesScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.last()
        val secondLastEntry = entries[entries.size - 2]

        return if (lastEntry.metadata[DETAIL] == true && secondLastEntry.metadata[LIST] == true) {
            buildListDetailScene(secondLastEntry, lastEntry)
        } else if (lastEntry.metadata[THIRD_PANEL] == true && secondLastEntry.metadata[DETAIL] == true && entries.size >= 3) {
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
            weights = sceneWeights,
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
            weights = sceneWeights,
            previousEntries = listOf(previousEntry, firstEntry),
            key = Pair(firstEntry.contentKey, secondEntry.contentKey)
        )
    }

    private fun buildAdaptiveListScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.last()
        if (lastEntry.metadata[LIST] == true) {
            return AdaptiveSinglePaneScene(
                pane = lastEntry, previousEntries = entries.dropLast(1), key = lastEntry.contentKey
            )
        }

        return null
    }
}
