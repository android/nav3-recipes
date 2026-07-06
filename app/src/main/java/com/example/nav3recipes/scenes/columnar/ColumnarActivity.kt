package com.example.nav3recipes.scenes.columnar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import com.example.nav3recipes.ui.theme.Nav3RecipesTheme
import kotlinx.serialization.Serializable

enum class RouteType {
    DIRECTORY,
    ITEM
}

sealed interface AppRoute : NavKey {
    val id: String
    val name: String
    val type: RouteType
}

@Serializable
data class DirectoryRoute(
    override val id: String,
    override val name: String
) : AppRoute {
    override val type: RouteType = RouteType.DIRECTORY
}

@Serializable
data class ItemRoute(
    override val id: String,
    override val name: String
) : AppRoute {
    override val type: RouteType = RouteType.ITEM

    val content: String
        get() = "Content for $name (ID: $id)"
}

class ColumnarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            Nav3RecipesTheme {
                val rootDirectory = DirectoryRoute("root", "Root Directory")
                val backStack = rememberNavBackStack(rootDirectory)
                val navigator = remember(backStack) { Navigator(backStack) }
                val columnarStrategy = rememberColumnarSceneStrategy<NavKey>()

                SharedTransitionLayout {
                    NavDisplay(
                        backStack = backStack,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        onBack = navigator::goBack,
                        sceneStrategies = listOf(columnarStrategy),
                        sharedTransitionScope = this,
                        transitionSpec = slideAndFade(forward = true),
                        popTransitionSpec = slideAndFade(forward = false),
                        predictivePopTransitionSpec = { slideAndFade<NavKey>(forward = false)() },
                        entryProvider = entryProvider {
                            entry<DirectoryRoute>(metadata = ColumnarScene.directoryPane()) { route ->
                                val lastActiveId = (backStack.lastOrNull() as? AppRoute)?.id
                                DirectoryScreen(
                                    route = route,
                                    lastActiveId = lastActiveId,
                                    onNavigateToSubDir = { subDir ->
                                        navigator.navigate(from = route, to = subDir)
                                    },
                                    onNavigateToItem = { item ->
                                        navigator.navigate(from = route, to = item)
                                    }
                                )
                            }
                            entry<ItemRoute>(metadata = ColumnarScene.detailPane()) { route ->
                                ItemDetailScreen(
                                    route = route,
                                    onBack = navigator::goBack
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun <T : Any> slideAndFade(
    forward: Boolean
): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    val multiplier = if (forward) 1f else -1f
    (slideInHorizontally(
        initialOffsetX = { (it * 0.3f * multiplier).toInt() },
        animationSpec = tween(250)
    ) + fadeIn(animationSpec = tween(250))) togetherWith (slideOutHorizontally(
        targetOffsetX = { (-it * 0.3f * multiplier).toInt() },
        animationSpec = tween(250)
    ) + fadeOut(animationSpec = tween(250)))
}
