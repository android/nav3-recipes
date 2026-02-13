package com.example.nav3recipes.dialogscenedecorator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.window.core.layout.WindowSizeClass

fun DialogProperties.copy(
    dismissOnBackPress: Boolean = this.dismissOnBackPress,
    dismissOnClickOutside: Boolean = this.dismissOnClickOutside,
    securePolicy: SecureFlagPolicy = this.securePolicy,
    usePlatformDefaultWidth: Boolean = this.usePlatformDefaultWidth,
    decorFitsSystemWindows: Boolean = this.decorFitsSystemWindows,
    windowTitle: String = this.windowTitle
): DialogProperties = DialogProperties(
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    securePolicy = securePolicy,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    decorFitsSystemWindows = decorFitsSystemWindows,
    windowTitle = windowTitle
)

enum class DismissalBehavior {
    All, Single
}

/**
 * Configuration for the [DialogDecoratorScene].
 *
 * @property dialogProperties The [DialogProperties] used to configure the dialog.
 * @property backDismissalBehavior Whether all entries in the scene should be dismissed when the
 * back button is pressed. This configuration only applies if the
 * [DialogProperties.dismissOnBackPress] is set to `true`.
 *
 */
class DialogDecoratorSceneConfiguration(
    val dialogProperties: DialogProperties = DialogProperties(),
    val backDismissalBehavior: DismissalBehavior = DismissalBehavior.Single,
    val shape: Shape = RoundedCornerShape(16.dp),
) {
    fun toDialogProperties(): DialogProperties {
        // If the desired behavior is to not dismiss all, the DialogProperties passed to the Dialog
        // needs to be configured to not handle the back press itself.
        if (dialogProperties.dismissOnBackPress && backDismissalBehavior != DismissalBehavior.All) {
            return dialogProperties.copy(dismissOnBackPress = false)
        }

        return dialogProperties
    }

    fun shouldDismissSingleOnBackPress() =
        dialogProperties.dismissOnBackPress && backDismissalBehavior == DismissalBehavior.Single
}

/**
 * [DialogDecoratorScene] is an [OverlayScene] used by [DialogSceneDecoratorStrategy] to present
 * another [Scene] within a [Dialog].
 *
 * @property scene The [Scene] to be displayed within the dialog.
 * @property overlaidEntries The [NavEntry]s that are overlaid by the dialog.
 * @property dialogDecoratorSceneConfiguration The [DialogDecoratorSceneConfiguration] used to
 * configure the dialog scene.
 * @property onBack The callback to be invoked when a back event should be handled
 * @property onDismissAll The callback to be invoked when the entire dialog stack should be dismissed
 **/
class DialogDecoratorScene<T : Any>(
    private val scene: Scene<T>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val dialogDecoratorSceneConfiguration: DialogDecoratorSceneConfiguration,
    private val onBack: () -> Unit,
    private val onDismissAll: () -> Unit
) : OverlayScene<T>, Scene<T> by scene {

    override val content: @Composable () -> Unit = {
        Dialog(
            onDismissRequest = onDismissAll,
            properties = dialogDecoratorSceneConfiguration.toDialogProperties()
        ) {
            Surface(
                shape = dialogDecoratorSceneConfiguration.shape
            ) {
                scene.content()
            }

            // Because back events are dispatched to the currently focused window, this back handler
            // must be contained within the dialog's content to receive the events.
            BackHandler(dialogDecoratorSceneConfiguration.shouldDismissSingleOnBackPress()) {
                onBack()
            }
        }
    }
}

@Composable
fun <T : Any> rememberDialogSceneDecoratorStrategy(
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    onDismissAll: ((List<NavEntry<T>>) -> Unit)
): DialogSceneDecoratorStrategy<T> = remember(windowSizeClass, onDismissAll) {
    DialogSceneDecoratorStrategy(windowSizeClass, onDismissAll = onDismissAll)
}

/**
 * A [SceneDecoratorStrategy] that wraps a [Scene] in a [DialogDecoratorScene] if the first
 * [NavEntry] within it has been marked with the [DialogSceneMetadataKey] and the window width
 * is at least [WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND].
 *
 * If you only need to display a single [NavEntry] in a [Dialog], using
 * [androidx.navigation3.scene.DialogSceneStrategy] instead may be preferable.
 *
 * @property windowSizeClass The current [WindowSizeClass] used to determine if dialogs should be used.
 * @property windowWidthDpBreakpoint the width in dp at or above which a dialog should be displayed.
 * @property onDismissAll callback invoked to dismiss all dialog entries, receives the entries that
 * are currently in the dialog.
 */
class DialogSceneDecoratorStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass,
    private val windowWidthDpBreakpoint: Int = WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
    private val onDismissAll: (List<NavEntry<T>>) -> Unit
) : SceneDecoratorStrategy<T> {

    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        if (!windowSizeClass.isWidthAtLeastBreakpoint(windowWidthDpBreakpoint)) return scene

        val dialogDecoratorSceneConfiguration =
            scene.entries.firstOrNull()?.metadata[DialogSceneMetadataKey] ?: return scene

        // This is critical to ensure that the scenes rendered beneath the dialog don't contain
        // any entries that are in the dialog.
        val overlaidEntries = scene.previousEntries.dropLastWhile { it in scene.entries }

        return DialogDecoratorScene(
            scene,
            overlaidEntries,
            dialogDecoratorSceneConfiguration,
            onBack,
            onDismissAll = { onDismissAll.invoke(scene.entries) })
    }

    companion object {
        object DialogSceneMetadataKey : NavMetadataKey<DialogDecoratorSceneConfiguration>

        fun sceneDialog(dialogDecoratorSceneConfiguration: DialogDecoratorSceneConfiguration = DialogDecoratorSceneConfiguration()): Map<String, Any> =
            metadata {
                put(DialogSceneMetadataKey, dialogDecoratorSceneConfiguration)
            }
    }
}
