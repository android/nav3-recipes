package com.example.nav3recipes.scenes.threepane

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.Scene
import androidx.navigation3.ui.SceneStrategy


// --- ThreePaneScene ---
/**
 * A custom [Scene] that displays three [NavEntry]s side-by-side in a 33/33/33 split.
 */
class ThreePaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val firstEntry: NavEntry<T>,
    val secondEntry: NavEntry<T>,
    val thirdEntry: NavEntry<T>
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(firstEntry, secondEntry, thirdEntry)
    override val content: @Composable (() -> Unit) = {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(0.33f)) {
                firstEntry.Content()
            }
            Column(modifier = Modifier.weight(0.33f)) {
                secondEntry.Content()
            }
            Column(modifier = Modifier.weight(0.33f)) {
                thirdEntry.Content()
            }
        }
    }

    companion object {
        internal const val THREE_PANE_KEY = "ThreePane"
        /**
         * Helper function to add metadata to a [NavEntry] indicating it can be displayed
         * in a three-pane layout.
         */
        fun threePane() = mapOf(THREE_PANE_KEY to true)
    }
}

// --- ThreePaneSceneStrategy ---
/**
 * A [SceneStrategy] that activates a [ThreePaneScene] if the window is wide enough
 * and the top three back stack entries declare support for three-pane display.
 */
class ThreePaneSceneStrategy<T : Any> : SceneStrategy<T> {
    @OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3WindowSizeClassApi::class) // Opt-in for adaptive and window size class APIs
    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (Int) -> Unit
    ): Scene<T>? {

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

        // Condition 1: Only return a Scene if the window is sufficiently wide to render three panes.
        // We use isWidthAtLeastBreakpoint with WIDTH_DP_LARGE_LOWER_BOUND (840dp).
        if (!windowSizeClass.isWidthAtLeastBreakpoint(840)) {
            return null
        }

        val lastThreeEntries = entries.takeLast(3)

        // Condition 2: Only return a Scene if there are three entries, and all have declared
        // they can be displayed in a three pane scene.
        return if (lastThreeEntries.size == 3
            && lastThreeEntries.all { it.metadata.containsKey(ThreePaneScene.THREE_PANE_KEY) }
        ) {
            val firstEntry = lastThreeEntries[0]
            val secondEntry = lastThreeEntries[1]
            val thirdEntry = lastThreeEntries[2]

            // The scene key must uniquely represent the state of the scene.
            // A Triple of the first, second, and third entry keys ensures uniqueness.
            val sceneKey = Triple(firstEntry.contentKey, secondEntry.contentKey, thirdEntry.contentKey)

            ThreePaneScene(
                key = sceneKey,
                // Where we go back to is a UX decision. In this case, we only remove the top
                // entry from the back stack, despite displaying three entries in this scene.
                // This is because in this app we only ever add one entry to the
                // back stack at a time. It would therefore be confusing to the user to add one
                // when navigating forward, but remove three when navigating back.
                previousEntries = entries.dropLast(2),
                firstEntry = firstEntry,
                secondEntry = secondEntry,
                thirdEntry = thirdEntry
            )

        } else {
            null
        }
    }
}
