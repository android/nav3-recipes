package com.example.nav3recipes.supportingpane

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.example.nav3recipes.bottomsheet.BottomSheetScene
import com.example.nav3recipes.scenes.twopane.TwoPaneScene

class SupportingPaneSceneStrategy<T: Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {
	@OptIn(ExperimentalMaterial3Api::class)
	override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
		if (entries.size < 2) return null

		val supportingEntry = entries.last()
		val primaryEntry = entries[entries.size - 2]

		return if (supportingEntry.metadata.contains(KEY) && primaryEntry.contentKey == supportingEntry.metadata[KEY]) {

			if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
				// Large screen - return two pane layout
				TwoPaneScene(
					key = "whatever",
					// Where we go back to is a UX decision. In this case, we only remove the top
					// entry from the back stack, despite displaying two entries in this scene.
					// This is because in this app we only ever add one entry to the
					// back stack at a time. It would therefore be confusing to the user to add one
					// when navigating forward, but remove two when navigating back.
					previousEntries = entries.dropLast(1),
					firstEntry = primaryEntry,
					secondEntry = supportingEntry
				)
			} else {
				// Mobile case - return just the supporting pane in a bottom sheet
				BottomSheetScene<T>(
					key = supportingEntry.contentKey as T,
					previousEntries = entries.dropLast(1),
					overlaidEntries = entries.dropLast(1),
					entry = supportingEntry,
					modalBottomSheetProperties = ModalBottomSheetProperties(),
					onBack = onBack
				)
			}
		} else {
			null
		}
	}

	companion object {
		const val KEY = "supportingPane"

		fun supportingPane(parent: Any) = mapOf(KEY to parent)

	}

}
