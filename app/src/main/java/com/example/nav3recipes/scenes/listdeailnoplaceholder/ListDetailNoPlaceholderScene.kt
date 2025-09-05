package com.example.nav3recipes.scenes.listdeailnoplaceholder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.Scene
import androidx.navigation3.ui.SceneStrategy
import androidx.window.core.layout.WindowSizeClass.Companion.BREAKPOINTS_V2
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import androidx.window.layout.WindowMetrics

@Composable
fun columnsBySize() : Int {
    val info = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass

    return when {
        info.isWidthAtLeastBreakpoint(WIDTH_DP_EXTRA_LARGE_LOWER_BOUND) -> 5
        info.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND) -> 4
        info.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> 3
        info.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) -> 2
        else -> 1
    }
}

class ListDetailNoPlaceholderScene<T : Any>(
    override val entries: List<NavEntry<T>>,
    override val previousEntries: List<NavEntry<T>>,
    override val key: Any,
    private val columns: Int
) : Scene<T> {

    override val content: @Composable (() -> Unit) = {
        if (previousEntries.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(0.5f)) {
                    previousEntries.last().Content()
                }
                Column(modifier = Modifier.weight(0.5f)) {
                    entries.last().Content()
                }
            }
        } else {
            entries.last().Content()
        }
    }

    companion object {
        internal const val LIST = "list"
        internal const val DETAIL = "detail"
        internal const val COLUMNS_KEY = "columns"

        fun list() = mapOf(LIST to true)
        fun detail() = mapOf(DETAIL to true)
        fun columns(count: Int) = mapOf(COLUMNS_KEY to count)
    }
}

class ListDetailNoPlaceholderSceneStrategy<T : Any> : SceneStrategy<T>{
    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (Int) -> Unit
    ): Scene<T>? {
        val columns = columnsBySize()

        if (entries.size >= 2) {
            val lastEntry = entries.last()
            val secondLastEntry = entries[entries.size - 2]
            if (lastEntry.metadata[ListDetailNoPlaceholderScene.DETAIL] == true &&
                secondLastEntry.metadata[ListDetailNoPlaceholderScene.LIST] == true) {
                return ListDetailNoPlaceholderScene(
                    entries = listOf(lastEntry),
                    previousEntries = listOf(secondLastEntry),
                    key = "list_detail_scene",
                    columns = columns
                )
            }
        }
        if (entries.isNotEmpty()) {
            val lastEntry = entries.last()
            if (lastEntry.metadata[ListDetailNoPlaceholderScene.LIST] == true) {
                return ListDetailNoPlaceholderScene(
                    entries = listOf(lastEntry),
                    previousEntries = emptyList(),
                    key = "list_only_scene",
                    columns = columns
                )
            }
        }
        return null
    }
}
