/*
 * Copyright 2025 The Android Open Source Project
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

package com.example.nav3recipes.migration.atomic.end

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.migration.content.ScreenA
import com.example.nav3recipes.migration.content.ScreenA1
import com.example.nav3recipes.migration.content.ScreenB
import com.example.nav3recipes.migration.content.ScreenB1
import com.example.nav3recipes.migration.content.ScreenC
import com.example.nav3recipes.multiplestacks.Navigator
import com.example.nav3recipes.multiplestacks.rememberNavigationState
import com.example.nav3recipes.ui.setEdgeToEdgeConfig


class EndAtomicMigrationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {

            val navigationState = rememberNavigationState(
                startRoute = RouteA,
                topLevelRoutes = TOP_LEVEL_ROUTES.keys
            )

            val navigator = remember { Navigator(navigationState) }

            val entryProvider = entryProvider {
                featureASection(
                    onSubRouteClick = { navigator.navigate(RouteA1) },
                    onDialogClick = { navigator.navigate(RouteD) },
                )
                featureBSection(
                    onDetailClick = { id -> navigator.navigate(RouteB1(id)) },
                    onDialogClick = { navigator.navigate(RouteD) },
                )
                featureCSection(
                    onDialogClick = { navigator.navigate(RouteD) },
                )
                entry<RouteD>(metadata = DialogSceneStrategy.dialog()) {
                    Text(
                        modifier = Modifier.background(Color.White),
                        text = "Route D title (dialog)"
                    )
                }
            }

            Scaffold(bottomBar = {
                NavigationBar {
                    TOP_LEVEL_ROUTES.forEach { (key, value) ->
                        val isSelected = key == navigationState.topLevelRoute
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navigator.navigate(key)
                            },
                            icon = {
                                Icon(
                                    imageVector = value.icon,
                                    contentDescription = value.description
                                )
                            },
                            label = { Text(value.description) }
                        )
                    }
                }
            })

            { paddingValues ->
                NavDisplay(
                    entries = navigationState.toDecoratedEntries(entryProvider),
                    onBack = { navigator.goBack() },
                    sceneStrategy = remember { DialogSceneStrategy() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

// Feature module A
private fun EntryProviderScope<NavKey>.featureASection(
    onSubRouteClick: () -> Unit,
    onDialogClick: () -> Unit
) {
    entry<RouteA> { ScreenA(onSubRouteClick, onDialogClick) }
    entry<RouteA1> { ScreenA1() }
}


// Feature module B
private fun EntryProviderScope<NavKey>.featureBSection(
    onDetailClick: (id: String) -> Unit,
    onDialogClick: () -> Unit
) {
    entry<RouteB> { ScreenB(onDetailClick, onDialogClick) }
    entry<RouteB1> { key -> ScreenB1(id = key.id) }
}

// Feature module C
private fun EntryProviderScope<NavKey>.featureCSection(
    onDialogClick: () -> Unit
) {
    entry<RouteC> { ScreenC(onDialogClick) }
}
