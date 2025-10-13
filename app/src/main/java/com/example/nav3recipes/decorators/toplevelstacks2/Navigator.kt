package com.example.nav3recipes.decorators.toplevelstacks2

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.SaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.write
import kotlinx.serialization.Serializable
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

@SuppressLint("RestrictedApi")
class Navigator(
    private var startRoute: Route,
    private var shouldPrintDebugInfo: Boolean = false,
) {

    val backStack = mutableStateListOf(startRoute)
    var topLevelRoute by mutableStateOf(startRoute)
        private set

    // Maintain a stack for each top level route
    private val topLevelStacks = mutableMapOf(startRoute to mutableStateListOf(startRoute))

    private val topLevelDecorators : MutableMap<Route, List<NavEntryDecorator<Route>>> = mutableMapOf()


    // Maintain a map of shared routes to their parent stacks
    private var sharedRoutes: MutableMap<Route, Route> = mutableMapOf()

    @Composable
    fun entries(entryProvider: ((Route) -> NavEntry<Route>)) : List<NavEntry<Route>> {

        val decorators = topLevelDecorators[topLevelRoute] ?: listOf(
            key(topLevelRoute) {
                rememberSaveableStateHolderNavEntryDecorator()
            }
        )
        println("Top level route: $topLevelRoute")
        println("Existing decorators: ${topLevelDecorators[topLevelRoute]}")
        println("Decorators: $decorators")

        topLevelDecorators[topLevelRoute] = decorators

        val entries =  rememberDecoratedNavEntries(
            backStack = topLevelStacks[topLevelRoute]!!,
            entryDecorators = decorators,
            entryProvider = entryProvider
        )

        if (topLevelRoute == startRoute){
            return entries
        } else {

            // Get the entries from the start route
            val startDecorators = topLevelDecorators[startRoute]!!
            val startEntries = rememberDecoratedNavEntries(
                backStack = topLevelStacks[startRoute]!!,
                entryDecorators = startDecorators,
                entryProvider = entryProvider
            )

            return startEntries + entries
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

    fun navlog(message: String) {
        if (shouldPrintDebugInfo) {
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

    private fun List<Route>.getDebugString(): String {
        val message = StringBuilder("[")
        forEach { entry ->
            message.append("Route: $entry, ")
        }
        message.append("]")
        return message.toString()
    }

    private fun addTopLevel(route: Route) {
        // Get the existing stack or create a new one.
        val topLevelStack = topLevelStacks[route] ?: mutableStateListOf(route)
        topLevelStacks.put(route, topLevelStack)
        navlog("Added top level route $route")
        topLevelRoute = route
    }

    private fun add(route: Route) {
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
    fun navigate(route: Route) {
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

    companion object {
        private const val KEY_START_ROUTE = "start_route"
        private const val KEY_CAN_TOP_LEVEL_ROUTES_EXIST_TOGETHER =
            "can_top_level_routes_exist_together"
        private const val KEY_SHOULD_PRINT_DEBUG_INFO = "should_print_debug_info"
        private const val KEY_TOP_LEVEL_ROUTE = "top_level_route"
        private const val KEY_TOP_LEVEL_STACK_IDS = "top_level_stack_ids"
        private const val KEY_TOP_LEVEL_STACK_KEY_PREFIX = "top_level_stack_key_"
        private const val KEY_TOP_LEVEL_STACK_VALUES_PREFIX = "top_level_stack_values_"
        private const val KEY_SHARED_ROUTES_KEYS = "shared_routes_keys"
        private const val KEY_SHARED_ROUTES_VALUES = "shared_routes_values"

        val Saver = Saver<Navigator, SavedState>(
            save = { navigator ->
                val savedState = SavedState()
                savedState.write {

                    putSavedState(KEY_START_ROUTE, encodeToSavedState(navigator.startRoute))
                    putBoolean(KEY_SHOULD_PRINT_DEBUG_INFO, navigator.shouldPrintDebugInfo)
                    putSavedState(KEY_TOP_LEVEL_ROUTE, encodeToSavedState(navigator.topLevelRoute))

                    // Create lists for each top level stack. Example:
                    // top_level_stack_ids = [1, 2, 3]
                    // top_level_stack_key_1 = [encodedStateA]
                    // top_level_stack_values_1 = [encodedStateA, encodedStateA1]
                    // top_level_stack_key_2 = ...

                    var id = 0
                    val ids = mutableListOf<Int>()

                    for ((key, stackValues) in navigator.topLevelStacks) {
                        putSavedState("$KEY_TOP_LEVEL_STACK_KEY_PREFIX$id", encodeToSavedState(key))
                        putSavedStateList(
                            "$KEY_TOP_LEVEL_STACK_VALUES_PREFIX$id",
                            stackValues.map { encodeToSavedState(it) })
                        ids.add(id)
                        id++
                    }

                    putIntList(KEY_TOP_LEVEL_STACK_IDS, ids)

                    val sharedRouteKeys = navigator.sharedRoutes.keys.toList()
                    val sharedRouteValues = navigator.sharedRoutes.values.toList()
                    putSavedStateList(
                        KEY_SHARED_ROUTES_KEYS,
                        sharedRouteKeys.map { encodeToSavedState(it) })
                    putSavedStateList(
                        KEY_SHARED_ROUTES_VALUES,
                        sharedRouteValues.map { encodeToSavedState(it) })
                }
                savedState
            },
            restore = { savedState ->
                savedState.read {
                    val restoredStartRoute =
                        decodeFromSavedState<Route>(getSavedState(KEY_START_ROUTE))
                    val restoredCanTopLevelRoutesExistTogether =
                        getBoolean(KEY_CAN_TOP_LEVEL_ROUTES_EXIST_TOGETHER)
                    val restoredShouldPrintDebugInfo = getBoolean(KEY_SHOULD_PRINT_DEBUG_INFO)

                    val navigator = Navigator(
                        startRoute = restoredStartRoute,
                        shouldPrintDebugInfo = restoredShouldPrintDebugInfo
                    )

                    navigator.topLevelRoute =
                        decodeFromSavedState(getSavedState(KEY_TOP_LEVEL_ROUTE))

                    val ids = getIntList(KEY_TOP_LEVEL_STACK_IDS)
                    for (id in ids) {
                        // get the key and the value list
                        val key: Route =
                            decodeFromSavedState(getSavedState("$KEY_TOP_LEVEL_STACK_KEY_PREFIX$id"))
                        val stackValues = getSavedStateList("$KEY_TOP_LEVEL_STACK_VALUES_PREFIX$id")
                            .map { decodeFromSavedState<Route>(it) }
                        navigator.topLevelStacks[key] = stackValues.toMutableStateList()
                    }

                    val encodedSharedRouteKeys = getSavedStateListOrNull(KEY_SHARED_ROUTES_KEYS)
                    val encodedSharedRouteValues = getSavedStateListOrNull(KEY_SHARED_ROUTES_VALUES)

                    if (encodedSharedRouteKeys != null &&
                        encodedSharedRouteValues != null &&
                        encodedSharedRouteKeys.size == encodedSharedRouteValues.size
                    ) {
                        val restoredKeys =
                            encodedSharedRouteKeys.map { decodeFromSavedState<Route>(it) }
                        val restoredValues =
                            encodedSharedRouteValues.map { decodeFromSavedState<Route>(it) }
                        navigator.sharedRoutes.clear()
                        for (i in restoredKeys.indices) {
                            navigator.sharedRoutes[restoredKeys[i]] = restoredValues[i]
                        }
                    }
                    navigator.updateBackStack()
                    navigator
                }
            }
        )
    }
}

@Composable
fun rememberNavigator(
    startRoute: Route,
    shouldPrintDebugInfo: Boolean = false
) = rememberSaveable(saver = Navigator.Saver) {
    Navigator(
        startRoute = startRoute,
        shouldPrintDebugInfo = shouldPrintDebugInfo
    )
}

@Serializable
sealed class Route {
    sealed class TopLevel : Route()
    sealed class Shared : Route()
}
