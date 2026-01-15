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

package com.example.nav3recipes.multiplestacksandauthentication

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable

sealed interface Root: NavKey

sealed interface Authentication: NavKey

@Serializable
data object AuthenticationRoute : Root {
    @Serializable
    data object WelcomeRoute : Authentication

    @Serializable
    data object LoginRoute : Authentication

    @Serializable
    data object RegisterRoute : Authentication
}

sealed interface Connected: NavKey

@Serializable
data object ConnectedRoute : Root {
    @Serializable
    data object RouteA : Connected

    @Serializable
    data object RouteA1 : Connected

    @Serializable
    data object RouteB : Connected

    @Serializable
    data object RouteB1 : Connected

    @Serializable
    data object RouteC : Connected

    @Serializable
    data object RouteC1 : Connected
}

private val TOP_LEVEL_ROUTES = mapOf<NavKey, NavBarItem>(
    ConnectedRoute.RouteA to NavBarItem(icon = Icons.Default.Home, description = "Route A"),
    ConnectedRoute.RouteB to NavBarItem(icon = Icons.Default.Face, description = "Route B"),
    ConnectedRoute.RouteC to NavBarItem(icon = Icons.Default.Camera, description = "Route C"),
)

data class NavBarItem(
    val icon: ImageVector,
    val description: String
)

class MultipleStacksAndAuthenticationActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val navigationState = rememberNavigationState(
                rootStartRoute = AuthenticationRoute,
                authenticationStartRoute = AuthenticationRoute.WelcomeRoute,
                topLevelStartRoute = ConnectedRoute.RouteA,
                topLevelRoutes = TOP_LEVEL_ROUTES.keys
            )

            val navigator = remember { Navigator(navigationState) }

            Scaffold(bottomBar = {
                if (navigationState.currentRootDestination == ConnectedRoute) {
                    NavigationBar {
                        TOP_LEVEL_ROUTES.forEach { (key, value) ->
                            val isSelected = key == navigationState.topLevelRoute
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { navigator.navigate(key) },
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
                }
            }) {
                NavDisplay(
                    backStack = navigationState.rootBackStack,
                    entryProvider = entryProvider {
                        rootSection(
                            navigator = navigator
                        )
                    }
                )
            }
        }
    }
}