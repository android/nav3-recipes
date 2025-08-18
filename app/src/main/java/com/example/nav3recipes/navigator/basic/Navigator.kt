package com.example.nav3recipes.navigator.basic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

/**
 * This class models navigation behavior. It provides a back stack
 * as a Compose snapshot-state backed list that can be used with a `NavDisplay`.
 *
 * It supports a single level of nested navigation. Top level
 * routes can be defined using the `Route` class and setting
 * `isTopLevel` to `true`. It also supports shared routes.
 * These are routes that can be nested under multiple top level
 * routes, though only one instance of the route will ever be
 * present in the stack. Shared routes can be defined using
 * `Route.isShared`.
 *
 * The start route is always the first item in the back stack and
 * cannot be moved. Navigating to the start route removes all other
 * top level routes and their associated stacks.
 *
 * @param startRoute - The start route for the back stack.
 * @param canTopLevelRoutesExistTogether - Determines whether other
 * top level routes can exist together on the back stack. Default `false`,
 * meaning other top level routes (and their stacks) will be popped off
 * the back stack when navigating to a top level route.
 *
 * For example, if A, B and C are all top level routes:
 *
 * ```
 * val navigator = Navigator<Route>(startRoute = A) // back stack is [A]
 * navigator.navigate(B) // back stack [A, B]
 * navigator.navigate(C) // back stack [A, C] - B is popped before C is added
 *
 * When set to `true`, the resulting back stack would be [A, B, C]
 * ```
 *
 * @see `NavigatorTest`.
 */
class Navigator<T: Route>(
    private val startRoute: T,
    private val canTopLevelRoutesExistTogether: Boolean = false
) {

    val backStack = mutableStateListOf(startRoute)
    var topLevelRoute by mutableStateOf(startRoute)
        private set

    // Maintain a stack for each top level route
    private var topLevelStacks : LinkedHashMap<T, MutableList<T>> = linkedMapOf(
        startRoute to mutableListOf(startRoute)
    )

    // Maintain a map of shared routes to their parent stacks
    private var sharedRoutes : MutableMap<T, T> = mutableMapOf()

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }

    private fun navigateToTopLevel(route: T){

        if (route == startRoute){
            clearAllExceptStartStack()
        } else {

            // Get the existing stack or create a new one.
            val topLevelStack = topLevelStacks.remove(route) ?: mutableListOf(route)

            if (!canTopLevelRoutesExistTogether) {
                clearAllExceptStartStack()
            }

            topLevelStacks.put(route, topLevelStack)
        }

        topLevelRoute = route
    }

    private fun clearAllExceptStartStack(){
        // Remove all other top level stacks, except the start stack
        val startStack = topLevelStacks[startRoute] ?: mutableListOf(startRoute)
        topLevelStacks.clear()
        topLevelStacks.put(startRoute, startStack)
    }

    /**
     * Navigate to the given route.
     */
    fun navigate(route: T){
        if (route.isTopLevel){
            navigateToTopLevel(route)
        } else {
            if (route.isShared){
                // If the key is already in a stack, remove it
                val oldParent = sharedRoutes[route]
                if (oldParent != null) {
                    topLevelStacks[oldParent]?.remove(route)
                }
                sharedRoutes[route] = topLevelRoute
            }
            topLevelStacks[topLevelRoute]?.add(route)
        }
        updateBackStack()
    }

    /**
     * Go back to the previous route.
     */
    fun goBack(){
        if (backStack.size <= 1){
            return
        }
        val removedKey = topLevelStacks[topLevelRoute]?.removeLastOrNull()
        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        topLevelRoute = topLevelStacks.keys.last()
        updateBackStack()
    }
}

@Serializable
abstract class Route(
    val isTopLevel : Boolean = false,
    val isShared : Boolean = false
)
