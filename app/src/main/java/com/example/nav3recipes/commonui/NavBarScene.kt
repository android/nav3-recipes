package com.example.nav3recipes.commonui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope


class NavBarSceneStrategy<T : Any>(
    val topLevelBackStack: TopLevelBackStack<Any>,
) : WrappedSceneStrategy<T>() {

    val isNavBarVisible: MutableState<Boolean> = mutableStateOf(true)

    override fun SceneStrategyScope<T>.calculateScene(scene: Scene<T>): Scene<T> {
        // Receive the Scene that NavDisplay is going to render
        // Always need to return a scene here
        val shouldShow = scene.metadata[NAV_BAR_KEY_IS_VISIBLE] as? Boolean ?: true

        return if (shouldShow) {
            NavBarScene(topLevelBackStack, scene, isNavBarVisible)
        } else {
            scene
        }
    }

    companion object {

        const val NAV_BAR_KEY_IS_VISIBLE = "isVisible"

        fun isVisible(isVisible: Boolean): Map<String, Any> =
            mapOf(NAV_BAR_KEY_IS_VISIBLE to isVisible)
    }

}

class NavBarScene<T : Any>(
    val topLevelBackStack: TopLevelBackStack<Any>,
    scene: Scene<T>,
    isVisible: MutableState<Boolean>
) : WrappedScene<T>(scene) {

    override val content: @Composable (() -> Unit) = {

        Box {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    scene.content()
                }
                AnimatedContent(isVisible) { isVisibleInternal ->
                    if (isVisibleInternal.value) {
                        NavigationBar {
                            TOP_LEVEL_ROUTES.forEach { topLevelRoute ->

                                val isSelected = topLevelRoute == topLevelBackStack.topLevelKey
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        topLevelBackStack.addTopLevel(topLevelRoute)
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = topLevelRoute.icon,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}


// Abstract class for the SceneStrategy
abstract class WrappedSceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        // Terminating function
        return null
    }
}

// Abstract class for the Scene
abstract class WrappedScene<T : Any>(val scene: Scene<T>) : Scene<T> {

    override val key: Any = scene.key
    override val entries: List<NavEntry<T>> = scene.entries
    override val previousEntries: List<NavEntry<T>> = scene.previousEntries
}