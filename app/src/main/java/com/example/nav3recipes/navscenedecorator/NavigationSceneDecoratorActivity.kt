package com.example.nav3recipes.navscenedecorator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.scenes.listdetail.rememberListDetailSceneStrategy
import com.example.nav3recipes.ui.setEdgeToEdgeConfig

class NavigationSceneDecoratorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEdgeToEdgeConfig()

        setContent {
            SharedTransitionLayout {
                val navigationState = rememberNavigationState(
                    startRoute = RouteA,
                    topLevelRoutes = setOf(RouteA, RouteB, RouteC)
                )

                val navigator = remember(navigationState) { Navigator(navigationState) }

                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

                val navigationSceneDecoratorStrategy =
                    rememberNavigationSceneDecoratorStrategy<NavKey>(
                        navBar = { NavBar(NAV_ITEMS, navigator) },
                        navRail = { NavRail(NAV_ITEMS, navigator) },
                        sharedTransitionScope = this
                    )

                val entryProvider = entryProvider {
                    featureASection { id -> navigator.navigate(RouteA1(id)) }
                    featureBSection { navigator.navigate(RouteB1) }
                    featureCSection { navigator.navigate(RouteC1) }
                }

                NavDisplay(
                    entries = navigationState.toEntries(entryProvider),
                    sceneDecoratorStrategies = listOf(navigationSceneDecoratorStrategy),
                    sceneStrategies = listOf(listDetailStrategy),
                    sharedTransitionScope = this,
                    onBack = navigator::goBack
                )
            }
        }
    }
}