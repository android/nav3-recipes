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

package com.example.nav3recipes.scenes.listdeailnoplaceholder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.navEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.example.nav3recipes.content.ContentBase
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.scenes.twopane.TwoPaneScene
import com.example.nav3recipes.scenes.twopane.TwoPaneSceneStrategy
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


class ListDetailNoPlaceholderActivity : ComponentActivity() {

    private val mockProducts = List(10) { Product(it) }

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            //val defaultNumberOfColumns = columnsBySize()
            val localNavSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
                compositionLocalOf {
                    throw IllegalStateException(
                        "Unexpected access to LocalNavSharedTransitionScope. You must provide a " +
                                "SharedTransitionScope from a call to SharedTransitionLayout() or " +
                                "SharedTransitionScope()"
                    )
                }


            var numberOfColumns by remember { mutableIntStateOf(1) }

            /**
             * A [NavEntryDecorator] that wraps each entry in a shared element that is controlled by the
             * [Scene].
             */
            val sharedEntryInSceneNavEntryDecorator = navEntryDecorator<NavKey> { entry ->
                with(localNavSharedTransitionScope.current) {
                    BoxWithConstraints(
                        Modifier.sharedElement(
                            rememberSharedContentState(entry.contentKey),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                        ),
                    ) {
                        if (entry.metadata.containsKey(ListDetailNoPlaceholderSceneStrategy.LIST)) {
                            numberOfColumns = columnsByComposableWidth(maxWidth)
                        }
                        entry.Content()
                    }
                }
            }


            val backStack = rememberNavBackStack(Home)
            val strategy =
                remember { ListDetailNoPlaceholderSceneStrategy<Any>(listInitialWeight = .5f) }

            SharedTransitionLayout {
                CompositionLocalProvider(localNavSharedTransitionScope provides this) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = { keysToRemove -> repeat(keysToRemove) { backStack.removeLastOrNull() } },
                        entryDecorators = listOf(
                            sharedEntryInSceneNavEntryDecorator,
                            rememberSceneSetupNavEntryDecorator(),
                            rememberSavedStateNavEntryDecorator()
                        ),
                        sceneStrategy = strategy,
                        entryProvider = entryProvider {
                            entry<Home>(
                                metadata = ListDetailNoPlaceholderSceneStrategy.Companion.list()
                            ) {
                                ContentRed("Adaptive List") {
                                    val gridCells = GridCells.Fixed(numberOfColumns)

                                    LazyVerticalGrid(
                                        columns = gridCells,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(mockProducts.size) {
                                            Text(
                                                text = "Product $it",
                                                modifier = Modifier
                                                    .padding(all = 16.dp)
                                                    .clickable {
                                                        backStack.addProductRoute(it)
                                                    })
                                        }
                                    }
                                }
                            }
                            entry<Product>(
                                metadata = ListDetailNoPlaceholderSceneStrategy.Companion.detail()
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
                        }
                    )
                }
            }
        }
    }

    private fun SnapshotStateList<NavKey>.addProductRoute(productId: Int) {
        val productRoute =
            Product(productId)
        // Avoid adding the same product route to the back stack twice.
        if (!contains(productRoute)) {
            add(productRoute)
        }
    }
}
