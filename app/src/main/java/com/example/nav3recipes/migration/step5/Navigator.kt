package com.example.nav3recipes.migration.step5

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.toRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Navigator that mirrors `NavController`'s back stack
 */
@SuppressLint("RestrictedApi")
internal class Navigator(
    coroutineScope: CoroutineScope,
    private val navController: NavHostController,
    private val startRoute: Any = Unit,
    private val canTopLevelRoutesExistTogether: Boolean = false,
    private val shouldPrintDebugInfo: Boolean = false
) {

    val backStack = mutableStateListOf(startRoute)
    var topLevelRoute by mutableStateOf(startRoute)
        private set

    // Maintain a stack for each top level route
    private lateinit var topLevelStacks : MutableMap<Any, MutableList<Any>>

    // Maintain a map of shared routes to their parent stacks
    private var sharedRoutes: MutableMap<Any, Any> = mutableMapOf()

    init {
        inititalizeTopLevelStacks()
        coroutineScope.launch {
            navController.currentBackStack.collect { nav2BackStack ->
                inititalizeTopLevelStacks()
                navlog("Top level stacks reset, parsing Nav2 back stack $nav2BackStack")
                printTopLevelStacks()

                nav2BackStack.forEach { entry ->
                    val destination = entry.destination

                    if (destination.navigatorName == "composable" || destination.navigatorName == "dialog"){
                        val route =
                            if (destination.hasRoute<RouteB>()) {
                                entry.toRoute<RouteB>()
                            } else if (destination.hasRoute<RouteB1>()) {
                                entry.toRoute<RouteB1>()
                            } else if (destination.hasRoute<RouteE>()) {
                                entry.toRoute<RouteE>()
                            } else {
                                // Non migrated top level route
                                entry
                            }
                        add(route)
                    } else {
                        navlog("Ignoring $entry")
                    }
                }
                printTopLevelStacks()
                updateBackStack()
            }
        }
    }

    private fun updateBackStack() {
        backStack.apply {
            clear()
            val entries = topLevelStacks.flatMap { it.value }
            addAll(entries)
        }
        printBackStack()
    }
    
    fun navlog(message: String){
        if (shouldPrintDebugInfo){
            println(message)
        }
    }

    private fun printBackStack() {
        navlog("Back stack: ${backStack.getDebugString()}")
    }

    private fun printTopLevelStacks() {

        navlog("Top level stacks: ")
        topLevelStacks.forEach { topLevelStack ->
            navlog("  ${topLevelStack.key} => ${topLevelStack.value.getDebugString()}")
        }
    }

    private fun List<Any>.getDebugString() : String {
        val message = StringBuilder("[")
        forEach { entry ->
            if (entry is NavBackStackEntry){
                message.append("Unmigrated route: ${entry.destination.route}, ")
            } else {
                message.append("Migrated route: $entry, ")
            }
        }
        message.append("]\n")
        return message.toString()
    }

    private fun addTopLevel(route: Any) {
        if (route == startRoute) {
            clearAllExceptStartStack()
        } else {

            // Get the existing stack or create a new one.
            val topLevelStack = topLevelStacks.remove(route) ?: mutableListOf(route)

            if (!canTopLevelRoutesExistTogether) {
                clearAllExceptStartStack()
            }

            topLevelStacks.put(route, topLevelStack)
            navlog("Added top level route $route")
        }
        topLevelRoute = route
    }

    private fun clearAllExceptStartStack() {
        // Remove all other top level stacks, except the start stack
        val startStack = topLevelStacks[startRoute] ?: mutableListOf(startRoute)
        topLevelStacks.clear()
        topLevelStacks.put(startRoute, startStack)
    }

    private fun inititalizeTopLevelStacks() {
        topLevelStacks = mutableMapOf(startRoute to mutableListOf(startRoute))
        topLevelRoute = startRoute
    }

    private fun add(route: Any) {
        navlog("Attempting to add $route")
        if (route is Route.TopLevel) {
            navlog("$route is a top level route")
            addTopLevel(route)
        } else {
            if (route is Route.Shared) {
                navlog("$route is a shared route")
                // If the key is already in a stack, remove it
                val oldParent = sharedRoutes[route]
                if (oldParent != null) {
                    topLevelStacks[oldParent]?.remove(route)
                }
                sharedRoutes[route] = topLevelRoute
            } else {
                navlog("$route is a normal route")
            }
            val hasBeenAdded = topLevelStacks[topLevelRoute]?.add(route) ?: false
            navlog("Added $route to $topLevelRoute stack: $hasBeenAdded")
        }
    }

    /**
     * Navigate to the given route.
     */
    fun navigate(route: Any, navOptions: NavOptions? = null) {
        navController.navigate(route, navOptions)

        // TODO: add instruction on when to uncomment this and remove the line above
        /*
        add(route)
        updateBackStack()
        */
    }

    /**
     * Go back to the previous route.
     */
    fun goBack() {
        navController.popBackStack()

        // TODO: add instruction on when to uncomment this and remove the line above
        /*
        if (backStack.size <= 1) {
            return
        }
        val removedKey = topLevelStacks[topLevelRoute]?.removeLastOrNull()
        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        topLevelRoute = topLevelStacks.keys.last()
        updateBackStack()
        */
    }

}

sealed interface Route {
    interface TopLevel : Route
    interface Shared : Route
}
