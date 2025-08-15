package com.example.nav3recipes.migration.step7

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions

/**
 * Navigator that mirrors `NavController`'s back stack
 */
@SuppressLint("RestrictedApi")
internal class Navigator(
    private val startRoute: Any = Unit,
    private val canTopLevelRoutesExistTogether: Boolean = false,
    private val shouldPrintDebugInfo: Boolean = false
) {

    val backStack = mutableStateListOf(startRoute)
    var topLevelRoute by mutableStateOf(startRoute)
        private set

    // Maintain a stack for each top level route
    private val topLevelStacks = mutableMapOf(startRoute to mutableListOf(startRoute))

    // Maintain a map of shared routes to their parent stacks
    private var sharedRoutes: MutableMap<Any, Any> = mutableMapOf()

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
        add(route)
        updateBackStack()
    }

    /**
     * Go back to the previous route.
     */
    fun goBack() {
        if (backStack.size <= 1) {
            return
        }
        val removedKey = topLevelStacks[topLevelRoute]?.removeLastOrNull()
        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        topLevelRoute = topLevelStacks.keys.last()
        updateBackStack()
    }

}

sealed interface Route {
    interface TopLevel : Route
    interface Shared : Route
}
