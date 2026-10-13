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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.NavigationBackHandler
import androidx.navigation3.scene.rememberNavigationEventState
import androidx.navigation3.scene.rememberSceneState
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.scenes.listdetail.rememberListDetailSceneStrategy
import com.example.nav3recipes.ui.setEdgeToEdgeConfig

class ResponsiveNavigationSceneDecoratorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEdgeToEdgeConfig()

        setContent {
            SharedTransitionLayout {
                val topLevelRoutes = remember { setOf(RouteA, RouteB, RouteC) }
                val navigationState = rememberNavigationState(
                    startRoute = RouteA,
                    topLevelRoutes = topLevelRoutes,
                )

                val navigator = remember(navigationState) { Navigator(navigationState) }

                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

                val responsiveNavigationSceneDecoratorStrategy =
                    rememberResponsiveNavigationSceneDecoratorStrategy<NavKey>(
                        navBar = { NavBar(NAV_ITEMS, navigator) },
                        navRail = { NavRail(NAV_ITEMS, navigator) },
                        sharedTransitionScope = this,
                    )

                val entryProvider = entryProvider {
                    featureASection { id -> navigator.navigate(RouteA1(id)) }
                    featureBSection { navigator.navigate(RouteB1) }
                    featureCSection { navigator.navigate(RouteC1) }
                }

                val sceneState = rememberSceneState(
                    entries = navigationState.toEntries(entryProvider),
                    sceneStrategies = listOf(listDetailStrategy),
                    sceneDecoratorStrategies = listOf(responsiveNavigationSceneDecoratorStrategy),
                    sharedTransitionScope = this,
                    onBack = navigator::goBack,
                )

                val navigationEventState = rememberNavigationEventState(sceneState)

                NavigationBackHandler(
                    sceneState = sceneState,
                    state = navigationEventState,
                    onBackCompleted = navigator::goBack,
                )

                val currentSceneIsDecorated =
                    sceneState.currentScene is DecoratedResponsiveScene<*>

                val topLevelContentKeys = remember(topLevelRoutes) {
                    topLevelRoutes.mapTo(mutableSetOf()) { it.toContentKey() }
                }

                CompositionLocalProvider(LocalCurrentSceneIsDecorated provides currentSceneIsDecorated) {
                    NavDisplay(
                        sceneState = sceneState,
                        navigationEventState = navigationEventState,
                        transitionSpec = {
                            if (isSameHierarchy(topLevelContentKeys)) {
                                materialSharedAxisXForward()
                            } else {
                                materialTopLevelCrossfade()
                            }
                        },
                        popTransitionSpec = {
                            if (isSameHierarchy(topLevelContentKeys)) {
                                materialSharedAxisXPop()
                            } else {
                                materialTopLevelCrossfade()
                            }
                        },
                        predictivePopTransitionSpec = {
                            if (isSameHierarchy(topLevelContentKeys)) {
                                materialSharedAxisXPop()
                            } else {
                                materialTopLevelCrossfade()
                            }
                        },
                    )
                }
            }
        }
    }
}