/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

data class ResponsiveNavigationScene<T : Any>(
    private val scene: Scene<T>,
    private val sharedTransitionScope: SharedTransitionScope,
    private val windowSizeClass: WindowSizeClass,
    private val navBarContent: @Composable (() -> Unit),
    private val navRailContent: @Composable (() -> Unit),
) : Scene<T> by scene {
    override val key = scene::class to scene.key

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
                            .sharedElement(
                                rememberSharedContentState("nav-rail"),
                                animatedContentScope
                            )
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
                            .sharedElement(
                                rememberSharedContentState("nav-bar"),
                                animatedContentScope
                            )
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
fun <T : Any> rememberResponsiveNavigationSceneDecoratorStrategy(
    navBar: @Composable () -> Unit,
    navRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
): ResponsiveNavigationSceneDecoratorStrategy<T> {
    val currentNavBar by rememberUpdatedState(navBar)
    val currentNavRail by rememberUpdatedState(navRail)

    val movableNavBar = remember { movableContentOf { currentNavBar() } }
    val movableNavRail = remember { movableContentOf { currentNavRail() } }

    return remember(windowSizeClass, sharedTransitionScope) {
        ResponsiveNavigationSceneDecoratorStrategy(
            windowSizeClass,
            sharedTransitionScope,
            movableNavBar,
            movableNavRail
        )
    }
}

class ResponsiveNavigationSceneDecoratorStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass,
    private val sharedTransitionScope: SharedTransitionScope,
    private val navBarContent: @Composable () -> Unit,
    private val navRailContent: @Composable () -> Unit,
) : SceneDecoratorStrategy<T> {

    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        return ResponsiveNavigationScene(
            scene,
            sharedTransitionScope,
            windowSizeClass,
            navBarContent,
            navRailContent
        )
    }

}