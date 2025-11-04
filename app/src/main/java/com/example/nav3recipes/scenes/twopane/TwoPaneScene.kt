package com.example.nav3recipes.scenes.twopane

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Transition
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigationevent.NavigationEventHistory
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND


// --- TwoPaneScene ---
/**
 * A custom [Scene] that displays two [NavEntry]s side-by-side in a 50/50 split.
 */
class TwoPaneScene<T : Any>(
    override val key: TwoPaneSceneKey,
    private val sceneBackStack: List<NavEntry<T>>,
    private val maxSimultaneousEntries: () -> Int,
    private val actualBackStackSize: () -> Int,
    onBack: () -> Unit,
) : Scene<T> {

    override val previousEntries: List<NavEntry<T>> =
        sceneBackStack.dropLast(1)

    override val entries: List<NavEntry<T>>
        get() = sceneBackStack.takeLast(maxSimultaneousEntries()).let { navEntries ->
            when {
                navEntries.size < 2 -> return@let navEntries
                navEntries.all { it.metadata.containsKey(TWO_PANE_KEY) } -> navEntries
                else -> navEntries.takeLast(1)
            }
        }

    override val content: @Composable (() -> Unit) = {
        SlideToPopLayout(
            isCurrentScene = sceneBackStack.size == actualBackStackSize(),
            entries = entries,
            onBack = onBack,
        )
    }

    companion object {
        internal const val TWO_PANE_KEY = "TwoPane"

        /**
         * Helper function to add metadata to a [NavEntry] indicating it can be displayed
         * in a two-pane layout.
         */
        fun twoPane() = mapOf(TWO_PANE_KEY to true)
    }
}

@Composable
fun <T : Any> rememberTwoPaneSceneStrategy(
    backStackSize: () -> Int,
): TwoPaneSceneStrategy<T> {
    val windowSizeClass = rememberUpdatedState(currentWindowAdaptiveInfo().windowSizeClass)

    return remember(windowSizeClass) {
        TwoPaneSceneStrategy(
            windowSizeClass = windowSizeClass::value,
            backStackSize = backStackSize,
        )
    }
}


// --- TwoPaneSceneStrategy ---
/**
 * A [SceneStrategy] that activates a [TwoPaneScene] if the window is wide enough
 * and the top two back stack entries declare support for two-pane display.
 */
class TwoPaneSceneStrategy<T : Any>(
    private val windowSizeClass: () -> WindowSizeClass,
    private val backStackSize: () -> Int,
) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>
    ): Scene<T> {
        val contentKeys = entries.map { it.contentKey }

        return TwoPaneScene(
            key = TwoPaneSceneKey(
                contentKeys = contentKeys,
            ),
            maxSimultaneousEntries = {
                if (windowSizeClass().isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) 2
                else 1
            },
            sceneBackStack = entries.toList(),
            actualBackStackSize = backStackSize,
            onBack = onBack,
        )
    }
}

/**
 * Scene key with a flag if it was created for a predictive back gesture.
 * NOTE: its equals and hashcode are completely independent of this predictive back flag.
 * This is to let [NavDisplay] find the appropriate scene to go back to with this key. The
 * flag is only used internally.
 */
data class TwoPaneSceneKey(
    val contentKeys: List<Any>,
)

@Composable
fun rememberStickyAnimatedVisibilityScope(): StickyAnimatedVisibilityScope {
    val navigationEventStatusState = rememberNavigationEventStatus()
    val animatedContentScope = LocalNavAnimatedContentScope.current

    return remember(navigationEventStatusState, animatedContentScope) {
        StickyAnimatedVisibilityScope(
            backStatus = navigationEventStatusState::value,
            animatedContentScope = animatedContentScope,
        )
    }
}

class StickyAnimatedVisibilityScope(
    val backStatus: () -> NavigationEventStatus,
    animatedContentScope: AnimatedContentScope,
) : AnimatedVisibilityScope by animatedContentScope {

    val isStickySharedElementVisible: Boolean
        get() = if (isShowingBackContent) !isEntering else isEntering

    private val isEntering
        get() = transition.targetState == EnterExitState.Visible

    private val isShowingBackContent: Boolean
        get() {
            val currentSize = transition.sceneCurrentDestinationKey?.contentKeys?.size ?: 0
            val targetSize = transition.sceneTargetDestinationKey?.contentKeys?.size ?: 0

            val animationIsSettling = targetSize < currentSize

            return when (backStatus()) {
                NavigationEventStatus.Seeking -> animationIsSettling
                NavigationEventStatus.Completed.Cancelled -> animationIsSettling
                NavigationEventStatus.Completed.Commited -> false
            }
        }

}

@Composable
fun rememberNavigationEventStatus(): State<NavigationEventStatus> {
    val navigationEventDispatcher = LocalNavigationEventDispatcherOwner.current!!
        .navigationEventDispatcher

    val lastSceneKey = remember {
        mutableStateOf<Any?>(null)
    }
    val navigationEventStatusState = remember {
        mutableStateOf<NavigationEventStatus>(NavigationEventStatus.Completed.Commited)
    }

    LaunchedEffect(navigationEventDispatcher) {
        navigationEventDispatcher.history.collect { history ->
            history.currentSceneKey?.let(lastSceneKey::value::set)
        }
    }

    LaunchedEffect(navigationEventDispatcher) {
        navigationEventDispatcher.transitionState.collect { transitionState ->
            navigationEventStatusState.value = when (transitionState) {
                NavigationEventTransitionState.Idle -> when (lastSceneKey.value) {
                    navigationEventDispatcher.history.value.currentSceneKey -> NavigationEventStatus.Completed.Cancelled
                    else -> NavigationEventStatus.Completed.Commited
                }

                is NavigationEventTransitionState.InProgress -> NavigationEventStatus.Seeking
            }
        }
    }
    return navigationEventStatusState
}

private val NavigationEventHistory.currentSceneKey
    get() = when (val navigationEventInfo = mergedHistory.getOrNull(currentIndex)) {
        is SceneInfo<*> -> navigationEventInfo.scene.key
        else -> null
    }

sealed class NavigationEventStatus {
    data object Seeking : NavigationEventStatus()
    sealed class Completed : NavigationEventStatus() {
        data object Commited : Completed()
        data object Cancelled : Completed()
    }
}

private val Transition<*>.sceneTargetDestinationKey: TwoPaneSceneKey
    get() {
        val target = parentTransition?.targetState as Scene<*>
        return target.key as TwoPaneSceneKey
    }

private val Transition<*>.sceneCurrentDestinationKey: TwoPaneSceneKey
    get() {
        val target = parentTransition?.currentState as Scene<*>
        return target.key as TwoPaneSceneKey
    }
