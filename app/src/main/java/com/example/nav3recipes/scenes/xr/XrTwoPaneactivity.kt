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

package com.example.nav3recipes.scenes.xr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentA
import com.example.nav3recipes.content.ContentB
import com.example.nav3recipes.scenes.listdetail.ProfileScreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable


@Serializable
data object FirstPane : NavKey

@Serializable
data object SecondPane : NavKey

@Serializable data object Profile : NavKey

class XrTwoPaneActivity : ComponentActivity() {

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {

            Scaffold { paddingValues ->

                val backStack = rememberNavBackStack(FirstPane)
                val xrTwoPaneSceneStrategy = rememberXrTwoPaneStrategy<NavKey>()
                val xrSinglePaneSceneStrategy = rememberXrSinglePaneStrategy<NavKey>()


                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategy = xrTwoPaneSceneStrategy then xrSinglePaneSceneStrategy,
                    modifier = Modifier.padding(paddingValues),
                    entryProvider = entryProvider {
                        entry<FirstPane>(
                            metadata = XrNavigationKeys.firstPane()
                        ) {
                            ContentA {
                                backStack.add(SecondPane)
                            }
                        }
                        entry<SecondPane>(
                            metadata = XrNavigationKeys.secondPane()
                        ) {
                            ContentB()
                        }
                        entry<Profile> {
                            ProfileScreen()
                        }
                    }
                )
            }
        }
    }
}
