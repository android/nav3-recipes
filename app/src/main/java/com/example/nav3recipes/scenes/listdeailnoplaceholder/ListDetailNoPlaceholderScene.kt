package com.example.nav3recipes.scenes.listdeailnoplaceholder

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

@Composable
fun columnsBySize(): Int {
    val info = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass

    return when {
        info.isWidthAtLeastBreakpoint(WIDTH_DP_EXTRA_LARGE_LOWER_BOUND) -> 5
        info.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND) -> 4
        info.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> 3
        info.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) -> 2
        else -> 1
    }
}

fun columnsByComposableWidth(width: Dp): Int {
    return when {
        width >= WIDTH_DP_EXTRA_LARGE_LOWER_BOUND.dp -> 5
        width >= WIDTH_DP_LARGE_LOWER_BOUND.dp -> 4
        width >= WIDTH_DP_EXPANDED_LOWER_BOUND.dp -> 3
        width >= WIDTH_DP_MEDIUM_LOWER_BOUND.dp -> 2
        else -> 1
    }
}

class ListDetailNoPlaceholderScene<T : Any>(
    val list: NavEntry<T>,
    val detail: NavEntry<T>,
    val listWeight: Float = 0.5f,
    val detailWeight: Float = 0.5f,
    override val previousEntries: List<NavEntry<T>>,
    override val key: Any
) : Scene<T> {

    override val entries: List<NavEntry<T>> = listOf(list, detail)

    override val content: @Composable (() -> Unit) = {

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(listWeight)) {
                list.Content()
            }
            Column(modifier = Modifier.weight(detailWeight)) {
                detail.Content()
            }
        }
    }
}

class ListNoPlaceholderScene<T : Any>(
    val list: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    override val key: Any
) : Scene<T> {

    override val entries: List<NavEntry<T>> = listOf(list)

    override val content: @Composable (() -> Unit) = {
        list.Content()
    }
}

class ListDetailNoPlaceholderSceneStrategy<T : Any>(val listInitialWeight: Float = 0.5f) :
    SceneStrategy<T> {

    companion object {
        internal const val LIST = "list"
        internal const val DETAIL = "detail"
        internal const val THIRD_PANEL = "thirdPanel"

        fun list() = mapOf(LIST to true)
        fun detail() = mapOf(DETAIL to true)

        fun thirdPanel() = mapOf(THIRD_PANEL to true)
    }

    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (Int) -> Unit
    ): Scene<T>? {

        if (listInitialWeight > 1f) {
            throw IllegalArgumentException("listInitialWeight must be less than or equal to 1f")
        }

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

        // Condition 1: Only return a Scene if the window is sufficiently wide to render two panes.
        // We use isWidthAtLeastBreakpoint with WIDTH_DP_MEDIUM_LOWER_BOUND (600dp).
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }

        //if(entries.size >= 3 && windowSizeClass.isWidthAtLeastBreakpoint())

        if (entries.size >= 2) {
            return buildTwoPaneScene(entries)
        }
        //Only the list is available
        if (entries.isNotEmpty()) {
            return buildAdaptiveListScene(entries)
        }
        return null
    }

    private fun buildTwoPaneScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.last()
        val secondLastEntry = entries[entries.size - 2]

        return if (lastEntry.metadata[DETAIL] == true &&
            secondLastEntry.metadata[LIST] == true
        ) {
            buildListDetailScene(secondLastEntry, lastEntry)
        } else if (lastEntry.metadata[THIRD_PANEL] == true &&
            secondLastEntry.metadata[DETAIL] == true && entries.size >= 3
        ) {
            val zeroethEntry = entries[entries.size - 3]
            buildDetailAndThirdPanelScene(secondLastEntry, lastEntry, zeroethEntry)
        } else {
            null
        }

    }

    private fun buildListDetailScene(firstEntry: NavEntry<T>, secondEntry: NavEntry<T>): Scene<T> {
        return ListDetailNoPlaceholderScene(
            list = firstEntry,
            detail = secondEntry,
            listWeight = listInitialWeight,
            detailWeight = 1f - listInitialWeight,
            previousEntries = listOf(firstEntry),
            key = Pair(firstEntry.contentKey, secondEntry.contentKey)
        )
    }

    private fun buildDetailAndThirdPanelScene(
        firstEntry: NavEntry<T>,
        secondEntry: NavEntry<T>,
        previousEntry: NavEntry<T>
    ): Scene<T> {
        return ListDetailNoPlaceholderScene(
            list = firstEntry,
            detail = secondEntry,
            listWeight = listInitialWeight,
            detailWeight = 1f - listInitialWeight,
            previousEntries = listOf(previousEntry, firstEntry),
            key = Pair(firstEntry.contentKey, secondEntry.contentKey)
        )
    }

    private fun buildAdaptiveListScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.last()
        if (lastEntry.metadata[LIST] == true) {
            return ListNoPlaceholderScene(
                list = lastEntry,
                previousEntries = entries.dropLast(1),
                key = lastEntry.contentKey
            )
        }

        return null
    }
}
