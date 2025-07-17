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

package com.example.nav3recipes.migration.start

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable

/**
 * Basic Navigation2 example with two screens. This will be the starting point for migration to
 * Navigation 3.
 */

@Serializable
private data object RouteA

@Serializable
private data class RouteB(val id: String)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            Scaffold { paddingValues ->
                NavHost(navController = navController, startDestination = RouteA, modifier = Modifier.padding(paddingValues)) {
                    composable<RouteA> {
                        Column {
                            Text("Route A")
                            Button(onClick = { navController.navigate(route = RouteB(id = "123")) }) {
                                Text("Go to B")
                            }
                        }
                    }
                    composable<RouteB> { key ->
                        Text("Route B: ${key.toRoute<RouteB>().id}")
                    }
                }
            }
        }
    }
}
