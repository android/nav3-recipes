package com.example.nav3recipes.genericoverlays

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.scene.OverlayScene as AndroidXOverlayScene

private class OverlayScene<T : Any>(
    override val key: Any,
    private val entry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
) : AndroidXOverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = { entry.Content() }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }
        other as OverlayScene<*>
        return key == other.key &&
                entry == other.entry &&
                previousEntries == other.previousEntries &&
                overlaidEntries == other.overlaidEntries
    }

    override fun hashCode(): Int =
        key.hashCode() * 31 +
                entry.hashCode() * 31 +
                previousEntries.hashCode() * 31 +
                overlaidEntries.hashCode() * 31

    override fun toString(): String =
        "OverlayScene(key=$key, entry=$entry, previousEntries=$previousEntries, overlaidEntries=$overlaidEntries)"
}

/**
 * A SceneStrategy that displays any entry marked with [overlay] as a generic overlay scene.
 *
 * This allows the entry itself to render dialogs, alert dialogs, or bottom sheets directly.
 */
class OverlaySceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.last()
        if (lastEntry.metadata[OVERLAY_KEY] != true) {
            return null
        }
        return OverlayScene(
            key = lastEntry.contentKey,
            entry = lastEntry,
            previousEntries = entries.dropLast(1),
            overlaidEntries = entries.dropLast(1),
        )
    }

    companion object {
        fun overlay(): Map<String, Any> = OVERLAY_METADATA

        private const val OVERLAY_KEY = "overlay"

        private val OVERLAY_METADATA = mapOf(OVERLAY_KEY to true)
    }
}