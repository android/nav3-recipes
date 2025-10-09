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

package com.example.nav3recipes.scenes.listdetailnoplaceholder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.navEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.example.nav3recipes.content.ContentBase
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import com.example.nav3recipes.ui.theme.colors
import kotlinx.serialization.Serializable

/**
 * This example shows how to create custom layouts using the Scenes API.
 *
 * A custom List Detail scene will render in the following way:
 *  - a single pane with a variable number of columns if no product has been selected
 *  - a list of products and the product detail, whenever a product is selected and the available
 *    window width is at least 600 dp
 *  - the product detail and an additional one if the window width is at least 600 dp
 *  - all three panes, when available, on bigger window size
 *
 *
 * @see `ListDetailNoPlaceholderScene`
 */
@Serializable
private object Home : NavKey

@Serializable
private data class Product(val id: Int) : NavKey

@Serializable
private data object Profile : NavKey

@Serializable
private data object Toolbar : NavKey


@OptIn(ExperimentalMaterial3Api::class)
class ListDetailNoPlaceholderActivity : ComponentActivity() {

    private val mockProducts = List(10) { Product(it) }

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {

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
                        if (entry.metadata.containsKey(ListDetailNoPlaceholderSceneStrategy.MAIN)) {
                            numberOfColumns = columnsByComposableWidth(maxWidth)
                        }
                        entry.Content()
                    }
                }
            }


            val backStack = rememberNavBackStack(Home)

            /**
             * A [SceneWeightsDefaults] that wraps variable initial weights to customise the appearance
             * of each panel
             */
            val defaults = ListDetailNoPlaceholderSceneStrategy.SceneDefaults()
                .copy(twoPanesScenePaneWeight = .4f)
            val strategy: SceneStrategy<NavKey> =
                remember { ListDetailNoPlaceholderSceneStrategy(defaults) }

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
                                metadata = ListDetailNoPlaceholderSceneStrategy.main()
                            ) {
                                ContentRed("Adaptive List") {
                                    val gridCells = GridCells.Fixed(numberOfColumns)

                                    LazyVerticalGrid(
                                        columns = gridCells,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
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

                                    Button(
                                        onClick = { backStack.addToolbar() },
                                        modifier = Modifier.padding(top = 32.dp)
                                    ) {
                                        Text("Open toolbar")
                                    }
                                }
                            }
                            entry<Product>(
                                metadata = ListDetailNoPlaceholderSceneStrategy.detail()
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
                            entry<Profile>(
                                metadata = ListDetailNoPlaceholderSceneStrategy.thirdPanel()
                            ) {
                                ContentGreen("Profile")
                            }

                            entry<Toolbar>(
                                metadata = ListDetailNoPlaceholderSceneStrategy.support()
                            ) {
                                ContentBlue("Toolbar")
                            }
                        }
                    )
                }
            }
        }
    }

    private fun NavBackStack<NavKey>.addProductRoute(productId: Int) {
        val productRoute =
            Product(productId)

        val lastItem = last()
        if (lastItem is Product || lastItem is Toolbar) {
            // Avoid adding the same product route to the back stack twice.
            if (lastItem == productRoute) {
                return
            } else {
                //Only have a single product as detail
                remove(lastItem)
                add(productRoute)
            }
        } else {
            add(productRoute)
        }
    }

    private fun NavBackStack<NavKey>.addToolbar() {

        val lastItem = last()
        if (lastItem is Product || lastItem is Toolbar) {
            remove(lastItem)
        }

        add(Toolbar)
    }

    /***
     * This function exemplify how to calculate the number of columns for the adaptive list based
     * on how much space is available inside the container
     */
    fun columnsByComposableWidth(width: Dp): Int {
        return when {
            width >= WIDTH_DP_EXTRA_LARGE_LOWER_BOUND.dp -> 5
            width >= WIDTH_DP_LARGE_LOWER_BOUND.dp -> 4
            width >= WIDTH_DP_EXPANDED_LOWER_BOUND.dp -> 3
            width >= WIDTH_DP_MEDIUM_LOWER_BOUND.dp -> 2
            else -> 1
        }
    }
}
