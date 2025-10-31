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

package com.example.nav3recipes.scenes.supportingpane

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.bottomsheet.BottomSheetSceneStrategy
import com.example.nav3recipes.content.ContentBase
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import com.example.nav3recipes.ui.theme.colors
import kotlinx.serialization.Serializable


@Serializable
private data object Home : NavKey

@Serializable
private data class Product(val id: Int) : NavKey

@Serializable
private data object Profile : NavKey

class SupportingPaneActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            val backStack = rememberNavBackStack(Home)
            val supportingPaneStrategy = rememberSupportingPaneSceneStrategy<NavKey>()
            //val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberMediatorNavEntryDecorator(),
                ),
                sceneStrategy = supportingPaneStrategy, //then bottomSheetStrategy,
                entryProvider = entryProvider {
                    entry<Home>(
                        clazzContentKey = { key -> key.toString() },
                        metadata = SupportingPaneSceneStrategy.primaryPane()
                    ) {
                        val viewModel = viewModel(modelClass = MediatorViewModel::class)

                        ContentRed("Welcome to Nav3") {
                            Column {
                                Button(onClick = {
                                    viewModel.count++
                                }){
                                    Text("Count: ${viewModel.count}")
                                }
                                Button(onClick = { backStack.addProductRoute(1) }) {
                                    Text("View the first product")
                                }
                            }
                        }
                    }
                    entry<Product>(
                        metadata = SupportingPaneSceneStrategy.supportingPane()
                        + MediatorNavEntryDecorator.viewModelParent(Home.toString()),
                    ) { product ->

                        val viewModel = viewModel(modelClass = MediatorViewModel::class)

                        ContentBase(
                            "Product ${product.id} ",
                            Modifier.background(colors[product.id % colors.size])
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                Button(onClick = {
                                    viewModel.count++
                                }){
                                    Text("Count: ${viewModel.count}")
                                }
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

private fun NavBackStack<NavKey>.addProductRoute(productId: Int) {
    val productRoute =
        Product(productId)
    // Avoid adding the same product route to the back stack twice.
    if (!contains(productRoute)) {
        add(productRoute)
    }
}


class MediatorViewModel : ViewModel() {

    var count by mutableIntStateOf(0)

}