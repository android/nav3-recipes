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

package com.example.nav3recipes.scenes.twopane

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateBounds
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.navEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBase
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import com.example.nav3recipes.ui.theme.colors
import kotlinx.serialization.Serializable

/**
 * This example shows how to create custom layouts using the Scenes API.
 *
 * A custom Scene, `TwoPaneScene`, will render content in two panes if:
 *
 * - the window width is over 600dp
 * - the last two nav entries on the back stack have indicated that they support being displayed in
 * a `TwoPaneScene` in their metadata.
 *
 *
 * @see `TwoPaneScene`
 */
@Serializable
private object Home : NavKey

@Serializable
private data class Product(val id: Int) : NavKey

@Serializable
private data object Profile : NavKey

class TwoPaneActivity : ComponentActivity() {

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            val backStack = rememberNavBackStack(Home)
            val twoPaneStrategy = remember { TwoPaneSceneStrategy<NavKey>() }

            SharedTransitionLayout {

                NavDisplay(
                    backStack = backStack,
                    onBack = { keysToRemove -> repeat(keysToRemove) { backStack.removeLastOrNull() } },
                    sceneStrategy = twoPaneStrategy,
                    entryProvider = entryProvider {
                        entry<Home>(
                            metadata = TwoPaneScene.twoPane()
                        ) {
                            ContentRed("Welcome to Nav3", modifier = Modifier.animateBounds(this@SharedTransitionLayout)) {
                                Button(onClick = { backStack.addProductRoute(1) }) {
                                    Text("View the first product")
                                }
                            }
                        }
                        entry<Product>(
                            metadata = TwoPaneScene.twoPane()
                        ) { product ->
                            ContentBase(
                                "Product ${product.id} ",
                                Modifier.background(colors[product.id % colors.size])
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Button(onClick = {
                                        backStack.addProductRoute(product.id + 1)
                                    }) {
                                        Text("View the next product")
                                    }
                                    Button(onClick = {
                                        backStack.add(Profile)
                                    }) {
                                        Text("View profile")
                                    }
                                }
                            }
                        }
                        entry<Profile> {
                            ContentGreen("Profile (single pane only)")
                        }
                    },
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) togetherWith
                                slideOutHorizontally(targetOffsetX = { -it })
                    },
                    popTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                                slideOutHorizontally(targetOffsetX = { it })
                    },
                    predictivePopTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                                slideOutHorizontally(targetOffsetX = { it })
                    }
                )

            }
        }
    }

    private fun NavBackStack<NavKey>.addProductRoute(productId: Int) {
        val productRoute =
            Product(productId)
        // Avoid adding the same product route to the back stack twice.
        if (!contains(productRoute)) {
            add(productRoute)
        }
    }
}
