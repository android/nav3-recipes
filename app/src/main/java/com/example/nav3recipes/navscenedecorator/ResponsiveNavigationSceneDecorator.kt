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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

/**
 * WindowInsets representing the insets of the navigation chrome (NavBar or NavRail).
 */
val LocalNavInsets: ProvidableCompositionLocal<WindowInsets> =
    staticCompositionLocalOf { WindowInsets(0, 0, 0, 0) }

/**
 * CompositionLocal indicating whether the currently displayed scene in NavDisplay is decorated
 * with [DecoratedResponsiveScene].
 */
val LocalCurrentSceneIsDecorated: ProvidableCompositionLocal<Boolean> =
    compositionLocalOf { true }

private val StationarySharedContentConfig =
    object : SharedTransitionScope.SharedContentConfig {
        override val permitTransformDuringDeferredTransition: Boolean = false
    }

private class LastInsets {
    private var l = -1
    private var r = -1
    private var b = -1

    fun changed(left: Int, right: Int, bottom: Int): Boolean {
        if (left == l && right == r && bottom == b) return false
        l = left
        r = right
        b = bottom
        return true
    }
}

/**
 * Strategy-scoped size cache so newly composed scenes during predictive back (`defer()`)
 * immediately know the last non-zero measured size of the shared nav slot and never overwrite
 * it with `0 x 0` during Lookahead when `!isMovableContentCaller`.
 */
internal class NavSlotSizeCache {
    var widthPx by mutableIntStateOf(0)
    var heightPx by mutableIntStateOf(0)
}

@Composable
fun <T : Any> rememberResponsiveNavigationSceneDecoratorStrategy(
    sharedTransitionScope: SharedTransitionScope,
    navBar: @Composable () -> Unit,
    navRail: @Composable () -> Unit,
): ResponsiveNavigationSceneDecoratorStrategy<T> {
    val currentNavBar by rememberUpdatedState(navBar)
    val currentNavRail by rememberUpdatedState(navRail)

    val movableNavBar = remember { movableContentOf { currentNavBar() } }
    val movableNavRail = remember { movableContentOf { currentNavRail() } }

    return remember(sharedTransitionScope) {
        ResponsiveNavigationSceneDecoratorStrategy(
            sharedTransitionScope = sharedTransitionScope,
            navBar = movableNavBar,
            navRail = movableNavRail,
        )
    }
}

internal class DecoratedResponsiveScene<T : Any>(
    private val scene: Scene<T>,
    private val sharedTransitionScope: SharedTransitionScope,
    private val navBar: @Composable () -> Unit,
    private val navRail: @Composable () -> Unit,
    private val navBarSizeCache: NavSlotSizeCache,
    private val navRailSizeCache: NavSlotSizeCache,
) : Scene<T> by scene {
    override val key: Any = scene::class to scene.key

    override val content: @Composable () -> Unit = {
        val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
        val showRail =
            windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

        val animatedContentScope = LocalNavAnimatedContentScope.current
        val currentSceneIsDecorated = LocalCurrentSceneIsDecorated.current
        val isMovableContentCaller =
            animatedContentScope.transition.targetState == EnterExitState.Visible ||
                !currentSceneIsDecorated

        ResponsiveNavigationScaffoldLayout(
            sharedTransitionScope = sharedTransitionScope,
            showRail = showRail,
            isMovableContentCaller = isMovableContentCaller,
            sizeCache = if (showRail) navRailSizeCache else navBarSizeCache,
            sharedElementKey =
                if (showRail) {
                    "ResponsiveNavigationSceneDecorator_NavRail"
                } else {
                    "ResponsiveNavigationSceneDecorator_NavBar"
                },
            navChrome = if (showRail) navRail else navBar,
            sceneContent = scene.content,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DecoratedResponsiveScene<*>) return false
        return scene == other.scene
    }

    override fun hashCode(): Int = scene.hashCode()
}

class ResponsiveNavigationSceneDecoratorStrategy<T : Any>(
    private val sharedTransitionScope: SharedTransitionScope,
    private val navBar: @Composable () -> Unit,
    private val navRail: @Composable () -> Unit,
) : SceneDecoratorStrategy<T> {

    internal val navBarSizeCache = NavSlotSizeCache()
    internal val navRailSizeCache = NavSlotSizeCache()

    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        // Dynamic Opt-Out / Opt-In: scene metadata takes precedence.
        // If not specified on the scene, check the top (last) entry first.
        val showNav =
            scene.metadata[NavSceneDecoratorKey]
                ?: scene.entries.lastOrNull { it.metadata[NavSceneDecoratorKey] != null }
                    ?.metadata?.get(NavSceneDecoratorKey)
                ?: true
        if (!showNav) return scene

        return DecoratedResponsiveScene(
            scene = scene,
            sharedTransitionScope = sharedTransitionScope,
            navBar = navBar,
            navRail = navRail,
            navBarSizeCache = navBarSizeCache,
            navRailSizeCache = navRailSizeCache,
        )
    }

    companion object {
        object NavSceneDecoratorKey : NavMetadataKey<Boolean>

        fun navScene(showNavigation: Boolean = true): Map<String, Any> =
            metadata {
                put(NavSceneDecoratorKey, showNavigation)
            }
    }
}

private const val ChromeLayoutId = "nav_chrome"
private const val ContentLayoutId = "scene_content"

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResponsiveNavigationScaffoldLayout(
    sharedTransitionScope: SharedTransitionScope,
    showRail: Boolean,
    isMovableContentCaller: Boolean,
    sizeCache: NavSlotSizeCache,
    sharedElementKey: String,
    navChrome: @Composable () -> Unit,
    sceneContent: @Composable () -> Unit,
) {
    val animatedContentScope = LocalNavAnimatedContentScope.current
    val chromeInsets = remember { MutableWindowInsets() }
    val lastInsets = remember { LastInsets() }

    with(sharedTransitionScope) {
        Layout(
            content = {
                // 1. Navigation Chrome slot (sharedElement + movableContent)
                Box(
                    modifier =
                        Modifier.layoutId(ChromeLayoutId)
                            .sharedElement(
                                sharedContentState =
                                    rememberSharedContentState(
                                        key = sharedElementKey,
                                        config = StationarySharedContentConfig,
                                    ),
                                animatedVisibilityScope = animatedContentScope,
                                zIndexInOverlay = 1f,
                            )
                ) {
                    if (isMovableContentCaller) {
                        navChrome()
                    }
                }

                // 2. Scene Content slot
                Box(modifier = Modifier.layoutId(ContentLayoutId)) {
                    CompositionLocalProvider(LocalNavInsets provides chromeInsets) {
                        sceneContent()
                    }
                }
            }
        ) { measurables, constraints ->
            val chromeMeasurable = measurables.first { it.layoutId == ChromeLayoutId }
            val contentMeasurable = measurables.first { it.layoutId == ContentLayoutId }

            val chromePlaceable = if (isMovableContentCaller) {
                val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
                val placeable = chromeMeasurable.measure(looseConstraints)
                if (isLookingAhead) {
                    sizeCache.widthPx = placeable.width
                    sizeCache.heightPx = placeable.height
                }
                placeable
            } else {
                val cachedWidth = sizeCache.widthPx
                val cachedHeight = sizeCache.heightPx
                if (cachedWidth > 0 || cachedHeight > 0) {
                    chromeMeasurable.measure(Constraints.fixed(cachedWidth, cachedHeight))
                } else {
                    chromeMeasurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                }
            }

            val chromeWidth = if (isMovableContentCaller) chromePlaceable.width else sizeCache.widthPx
            val chromeHeight = if (isMovableContentCaller) chromePlaceable.height else sizeCache.heightPx

            // Update measure-phase WindowInsets BEFORE measuring content
            val startPx = if (showRail) chromeWidth else 0
            val left = if (layoutDirection == LayoutDirection.Ltr) startPx else 0
            val right = if (layoutDirection == LayoutDirection.Rtl) startPx else 0
            val bottom = if (showRail) 0 else chromeHeight
            if (lastInsets.changed(left, right, bottom)) {
                chromeInsets.insets = WindowInsets(left = left, right = right, bottom = bottom)
            }

            // Measure scene content second — it reads the updated insets in the same pass
            val contentPlaceable = contentMeasurable.measure(constraints)

            layout(constraints.maxWidth, constraints.maxHeight) {
                contentPlaceable.placeRelative(0, 0)
                if (showRail) {
                    chromePlaceable.placeRelative(0, 0)
                } else {
                    chromePlaceable.placeRelative(0, constraints.maxHeight - chromeHeight)
                }
            }
        }
    }
}

private const val DURATION_MS = 300
private const val FADE_OUT_MS = 90
private const val FADE_IN_MS = 210
private const val SLIDE_OFFSET_DIVISOR = 5 // 20% width horizontal shift

/**
 * Material Fade Through / Crossfade for switching between unrelated top-level destinations.
 */
fun materialTopLevelCrossfade(): ContentTransform =
    (fadeIn(
        animationSpec = tween(durationMillis = FADE_IN_MS, delayMillis = FADE_OUT_MS, easing = LinearOutSlowInEasing)
    ) + scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(durationMillis = FADE_IN_MS, delayMillis = FADE_OUT_MS, easing = LinearOutSlowInEasing)
    )) togetherWith fadeOut(
        animationSpec = tween(durationMillis = FADE_OUT_MS, easing = FastOutLinearInEasing)
    )

/**
 * Material Shared Axis X (Forward) for navigating deeper within a hierarchy.
 */
fun materialSharedAxisXForward(): ContentTransform =
    (slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / SLIDE_OFFSET_DIVISOR },
        animationSpec = tween(durationMillis = DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(durationMillis = FADE_IN_MS, delayMillis = FADE_OUT_MS, easing = LinearOutSlowInEasing)
    )) togetherWith (slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / SLIDE_OFFSET_DIVISOR },
        animationSpec = tween(durationMillis = DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(durationMillis = FADE_OUT_MS, easing = FastOutLinearInEasing)
    ))

/**
 * Material Shared Axis X (Pop) for navigating back up within a hierarchy.
 */
fun materialSharedAxisXPop(): ContentTransform =
    (slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / SLIDE_OFFSET_DIVISOR },
        animationSpec = tween(durationMillis = DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(durationMillis = FADE_IN_MS, delayMillis = FADE_OUT_MS, easing = LinearOutSlowInEasing)
    )) togetherWith (slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth / SLIDE_OFFSET_DIVISOR },
        animationSpec = tween(durationMillis = DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(durationMillis = FADE_OUT_MS, easing = FastOutLinearInEasing)
    ))

// Matches Navigation 3's internal defaultContentKey(key) format
fun NavKey.toContentKey(): Any = "$this:${this::class}"

/**
 * Finds the contentKey of the top-level destination that owns this [Scene].
 */
fun Scene<*>.activeTopLevelContentKey(topLevelContentKeys: Set<Any>): Any? =
    (previousEntries + entries)
        .lastOrNull { it.contentKey in topLevelContentKeys }
        ?.contentKey

fun AnimatedContentTransitionScope<out Scene<*>>.isSameHierarchy(
    topLevelContentKeys: Set<Any>
): Boolean {
    val initialTopLevel = initialState.activeTopLevelContentKey(topLevelContentKeys)
    val targetTopLevel = targetState.activeTopLevelContentKey(topLevelContentKeys)
    return initialTopLevel != null && initialTopLevel == targetTopLevel
}

