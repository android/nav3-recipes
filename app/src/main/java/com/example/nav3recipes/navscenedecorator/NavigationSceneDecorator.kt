package com.example.nav3recipes.navscenedecorator

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.window.core.layout.WindowSizeClass

class NavigationScene<T : Any>(
    scene: Scene<T>,
    windowSizeClass: WindowSizeClass,
    sharedTransitionScope: SharedTransitionScope,
    navBarContent: @Composable (() -> Unit),
    navRailContent: @Composable (() -> Unit),
) : Scene<T> {
    override val key = scene::class to scene.key
    override val entries = scene.entries
    override val previousEntries = scene.previousEntries
    override val metadata = scene.metadata

    override val content = @Composable {
        val animatedContentScope = LocalNavAnimatedContentScope.current
        val isMovableContentCaller =
            animatedContentScope.transition.targetState == EnterExitState.Visible

        with(sharedTransitionScope) {
            if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
                Row(Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .cacheSize(!isMovableContentCaller)
                            .renderInSharedTransitionScopeOverlay()
                    ) {
                        if (isMovableContentCaller) {
                            navRailContent()
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        scene.content()
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        scene.content()
                    }
                    Box(
                        modifier = Modifier
                            .cacheSize(!isMovableContentCaller)
                            .renderInSharedTransitionScopeOverlay()
                    ) {
                        if (isMovableContentCaller) {
                            navBarContent()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T : Any> rememberNavigationSceneDecoratorStrategy(
    navBar: @Composable () -> Unit,
    navRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
): NavigationSceneDecoratorStrategy<T> {
    val currentNavBar by rememberUpdatedState(navBar)
    val currentNavRail by rememberUpdatedState(navRail)

    val movableNavBar = remember { movableContentOf { currentNavBar() } }
    val movableNavRail = remember { movableContentOf { currentNavRail() } }

    return remember(windowSizeClass, sharedTransitionScope) {
        NavigationSceneDecoratorStrategy(
            windowSizeClass,
            sharedTransitionScope,
            movableNavBar,
            movableNavRail
        )
    }
}

class NavigationSceneDecoratorStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass,
    private val sharedTransitionScope: SharedTransitionScope,
    private val navBarContent: @Composable () -> Unit,
    private val navRailContent: @Composable () -> Unit,
) : SceneDecoratorStrategy<T> {

    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        return NavigationScene(
            scene,
            windowSizeClass,
            sharedTransitionScope,
            navBarContent,
            navRailContent
        )
    }

}