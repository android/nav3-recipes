package com.example.nav3recipes.scenes.threepanes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.Scene
import androidx.navigation3.ui.SceneStrategy
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

internal data class ThreePaneScene<T : Any>(
    override val key: Any,
    override val entries: List<NavEntry<T>>,
    override val previousEntries: List<NavEntry<T>>,
) : Scene<T> {

    override val content: @Composable () -> Unit = {
        val (firstEntry, secondEntry, thirdEntry) = computeEntriesAndEmptyState()
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                firstEntry.Content()
            }

            val weight = if(thirdEntry != null) 1f else 2f
            if(secondEntry != null) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(weight)
                ) {
                    secondEntry.Content()
                }

                if(thirdEntry != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    ) {
                        thirdEntry.Content()
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                ) {
                    Text("Empty state")
                }
            }
        }
    }

    private fun computeEntriesAndEmptyState() : Triple<NavEntry<T>, NavEntry<T>?, NavEntry<T>?> {
        if(entries.size >= 3) {
            return Triple(entries[entries.lastIndex - 2], entries[entries.lastIndex - 1], entries.last())
        } else if(entries.size == 2) {
            return Triple(entries[entries.lastIndex - 1], entries.last(), null)
        } else {
            return Triple(entries.last(), null, null)
        }
    }

}

/**
 * A [SceneStrategy] that always creates a 1-entry [Scene] simply displaying the last entry in the
 * list.
 */
public class ThreePaneSceneStrategy<T : Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {
    @Composable
    override fun calculateScene(entries: List<NavEntry<T>>, onBack: (Int) -> Unit): Scene<T>? {
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND))
            return null
        return ThreePaneScene(
            key = entries.last().contentKey,
            entries = entries,
            previousEntries = entries.dropLast(3),
        )
    }
}

/*
@Composable
internal fun <T : Any> SceneStrategy<T>.calculateSceneWithSinglePaneFallback(
    entries: List<NavEntry<T>>,
    onBack: (count: Int) -> Unit,
): Scene<T> =
    calculateScene(entries, onBack) ?: ThreePaneSceneStrategy<T>().calculateScene(entries, onBack)
*/