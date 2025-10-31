package com.example.nav3recipes.scenes.supportingpane

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND


class SupportingPaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val firstEntry: NavEntry<T>,
    val secondEntry: NavEntry<T>
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(firstEntry, secondEntry)
    override val content: @Composable (() -> Unit) = {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(0.6f)) {
                firstEntry.Content()
            }
            Column(modifier = Modifier.weight(0.4f)) {
                secondEntry.Content()
            }
        }
    }


}

@Composable
fun <T: Any> rememberSupportingPaneSceneStrategy() : SupportingPaneSceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    return remember(windowSizeClass){
        SupportingPaneSceneStrategy(windowSizeClass)
    }
}


// --- SupportingPaneSceneStrategy ---
/**
 * A [SceneStrategy] that activates a [SupportingPaneScene] if the window is wide enough
 * and the top two back stack entries declare support for two-pane display.
 */
class SupportingPaneSceneStrategy<T : Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {

        // Condition 1: Only return a Scene if the window is sufficiently wide to render two panes.
        // We use isWidthAtLeastBreakpoint with WIDTH_DP_MEDIUM_LOWER_BOUND (600dp).
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }

        val supportingEntry =
            entries.lastOrNull()?.takeIf { it.metadata.containsKey(SUPPORTING_PANE_KEY) } ?: return null
        val primaryEntry = entries.findLast { it.metadata.containsKey(PRIMARY_PANE_KEY) } ?: return null


        return SupportingPaneScene(
            key = primaryEntry.contentKey,
            // Where we go back to is a UX decision. In this case, we only remove the top
            // entry from the back stack, despite displaying two entries in this scene.
            // This is because in this app we only ever add one entry to the
            // back stack at a time. It would therefore be confusing to the user to add one
            // when navigating forward, but remove two when navigating back.
            previousEntries = entries.dropLast(1),
            firstEntry = primaryEntry,
            secondEntry = supportingEntry
        )
    }

    companion object {
        internal const val PRIMARY_PANE_KEY = "PrimaryPane"
        internal const val SUPPORTING_PANE_KEY = "SupportingPane"
        fun primaryPane() = mapOf(PRIMARY_PANE_KEY to true)
        fun supportingPane() = mapOf(SUPPORTING_PANE_KEY to true)
    }

}
