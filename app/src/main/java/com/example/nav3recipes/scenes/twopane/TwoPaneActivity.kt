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
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.fromColorLong
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.example.nav3recipes.content.ContentBase
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.scenes.twopane.DragToPopState.Companion.dragToPop
import com.example.nav3recipes.scenes.twopane.DragToPopState.Companion.rememberDragToPopState
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import com.example.nav3recipes.ui.theme.colors
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlin.math.max

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
private data class Profile(
    val colorLong: Long,
) : NavKey

class TwoPaneActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            val backStack = rememberNavBackStack(Home)
            val twoPaneStrategy = rememberTwoPaneSceneStrategy<NavKey>(
                backStackSize = backStack::size
            )

            SharedTransitionLayout {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategy = twoPaneStrategy,
                    entryProvider = entryProvider {
                        entry<Home>(
                            metadata = TwoPaneScene.twoPane()
                        ) {
                            ContentRed(
                                title = "Welcome to Nav3",
                                modifier = Modifier
                            ) {
                                Button(onClick = { backStack.addProductRoute(1) }) {
                                    Text("View the first product")
                                }
                            }
                        }
                        entry<Product>(
                            metadata = TwoPaneScene.twoPane()
                        ) { product ->
                            ContentBase(
                                title = "Product ${product.id} ",
                                Modifier
                                    .background(colors[product.id % colors.size])
                            ) {
                                val stickyAnimatedVisibilityScope =
                                    rememberStickyAnimatedVisibilityScope()
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Button(onClick = {
                                        backStack.addProductRoute(product.id + 1)
                                    }) {
                                        Text("View the next product")
                                    }
                                    val profileColor = colors[(product.id + 3) % colors.size]
                                    OutlinedButton(onClick = {
                                        backStack.add(Profile(colorLong = profileColor.toColorLong()))
                                    }) {
                                        Box(
                                            modifier = Modifier
                                                .sharedElementWithCallerManagedVisibility(
                                                    sharedContentState = rememberSharedContentState(
                                                        profileColor,
                                                    ),
                                                    visible = stickyAnimatedVisibilityScope.isStickySharedElementVisible,
                                                )
                                                .background(
                                                    color = profileColor,
                                                    shape = CircleShape,
                                                )
                                                .size(24.dp)
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Text("View profile")
                                    }
                                }
                            }
                        }
entry<Profile>(
    metadata = NavDisplay.predictivePopTransitionSpec {
        // Update the pop back transition spec, the default scale
        // down will run on everything but the sticky shared element
        EnterTransition.None togetherWith fadeOut(targetAlpha = 0.8f)
    }
) { profile ->
    val stickyAnimatedVisibilityScope =
        rememberStickyAnimatedVisibilityScope()

    val scale = LocalNavigationEventDispatcherOwner.current!!
        .navigationEventDispatcher
        .transitionState
        .map(NavigationEventTransitionState::predictiveBackScale)
        .collectAsState(1f)

    ContentGreen(
        title = "Profile (single pane only)",
        modifier = Modifier
            .fillMaxSize(fraction = scale.value)
            .dragToPop(rememberDragToPopState())
    ) {
        val profileColor = Color.fromColorLong(profile.colorLong)
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(vertical = 16.dp)
                .sharedElementWithCallerManagedVisibility(
                    sharedContentState = rememberSharedContentState(
                        profileColor
                    ),
                    visible = stickyAnimatedVisibilityScope.isStickySharedElementVisible,
                )
                .background(
                    color = profileColor,
                    shape = CircleShape,
                )
                .size(80.dp)
        )
    }
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
    // Avoid adding the same product route to the back stack twice.
    if (!contains(productRoute)) {
        add(productRoute)
    }
}

private fun NavigationEventTransitionState.predictiveBackScale() = when (this) {
    NavigationEventTransitionState.Idle -> 1f
    is NavigationEventTransitionState.InProgress -> max(
        1f - latestEvent.progress,
        0.7f
    )
}

