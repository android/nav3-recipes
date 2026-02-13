package com.example.nav3recipes.dialogscenedecorator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.window.core.layout.WindowSizeClass

class DialogDecoratorScene<T : Any>(
    val scene: Scene<T>,
    override val overlaidEntries: List<NavEntry<T>>,
    val dialogProperties: DialogProperties,
    val onBack: () -> Unit
) : OverlayScene<T> {
    override val key = scene.key
    override val entries = scene.entries
    override val previousEntries = overlaidEntries

    override val content: @Composable (() -> Unit) = {
        Dialog(
            onDismissRequest = { repeat(entries.size) { onBack() } },
            properties = dialogProperties
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
            ) {
                scene.content()
            }

            // Because back events are dispatched to the currently focused window, this back handler
            // must be contained within the dialog\'s content to receive the events.
            BackHandler(!dialogProperties.dismissOnBackPress) {
                onBack()
            }
        }
    }
}

@Composable
fun <T : Any> rememberDialogSceneDecoratorStrategy(): DialogSceneDecoratorStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    return remember(windowSizeClass) {
        DialogSceneDecoratorStrategy<T>(windowSizeClass)
    }
}

/**
 * A [SceneDecoratorStrategy] that wraps a [Scene] in a [DialogDecoratorScene] if the first
 * [NavEntry] within it has been marked with the [DialogSceneMetadataKey] and the window width
 * is at least [WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND].
 *
 * @param T The type of the destination key.
 * @property windowSizeClass The current [WindowSizeClass] used to determine if dialogs should be used.
 */
class DialogSceneDecoratorStrategy<T : Any>(val windowSizeClass: WindowSizeClass) :
    SceneDecoratorStrategy<T> {

    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) return scene

        val dialogProperties = scene.entries[0].metadata[DialogSceneMetadataKey] ?: return scene

        // This is critical to ensure that the scenes rendered beneath the dialog don't contain
        // any entries that are in the dialog.
        val overlaidEntries = scene.previousEntries.takeWhile { it !in scene.entries }

        return DialogDecoratorScene(
            scene,
            overlaidEntries,
            dialogProperties,
            onBack
        )
    }

    companion object {
        object DialogSceneMetadataKey : NavMetadataKey<DialogProperties>

        fun sceneDialog(dialogProperties: DialogProperties = DialogProperties()): Map<String, Any> =
            metadata {
                put(DialogSceneMetadataKey, dialogProperties)
            }
    }
}
