@file:SuppressLint("RestrictedApi")

package com.example.nav3recipes.modular.hilt

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.core.util.Consumer
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkDslBuilder
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavGraphBuilder
import androidx.navigation.createGraph
import androidx.navigation.navigation
import com.example.nav3recipes.navigator.Route

private const val TAG = "NavDeeplinkHandler"

@Composable
fun NavDeeplinkHandler(
    savedInstanceState: Bundle?,
    routeBuilder: RouteRegistry.() -> Unit,
    onDeepLink: (Route) -> Unit
) {
    // backStack backed up SnapshotStateList so we should be able to track its changes
    val activity = LocalActivity.current as? ComponentActivity
    if (activity != null) {
        var routeRegistry by remember { mutableStateOf<RouteRegistry?>(null) }
        val graph = remember {
            NavController(activity).let {
                val graph = it.createGraph("?", null, {
                    routeRegistry = RouteRegistryImpl(this).apply(routeBuilder)
                })
                graph
            }
        }
        val handleDeepLink: (Uri) -> Unit by rememberUpdatedState { uri ->
            val deepLinkMatch = graph.matchDeepLinkComprehensive(
                navDeepLinkRequest = NavDeepLinkRequest(uri, null, null),
                searchChildren = true,
                searchParent = true,
                lastVisited = graph
            )
            val destination = deepLinkMatch?.destination
            val destinationRoute = destination?.route
            val args = deepLinkMatch?.matchingArgs
            destinationRoute?.let { route ->
                routeRegistry?.buildRoute(route, args)?.let { destination ->
                    Log.d(TAG, "navigating to $destination")
                    onDeepLink(destination)
                }
            }
            Log.d(TAG, "deepLinkMatch=$deepLinkMatch, args=$args, route=$destinationRoute")
        }
        if (savedInstanceState == null) {
            LaunchedEffect(Unit) {
                activity.intent?.data?.let { handleDeepLink(it) }
            }
        }
        DisposableEffect(activity) {
            val listener = Consumer<Intent> { intent ->
                // we are consuming uri from the PendingIntent fired via NotificationManager
                intent.data?.let { handleDeepLink(it) }
            }
            activity.addOnNewIntentListener(listener)
            onDispose { activity.removeOnNewIntentListener(listener) }
        }
    }
}

interface RouteRegistry {
    fun register(
        route: String,
        navDeepLink: NavDeepLinkDslBuilder.() -> Unit,
        routeFactory: (Bundle) -> Route
    )

    fun buildRoute(route: String, args: Bundle?): Route?
}

private class RouteRegistryImpl(
    private val navGraphBuilder: NavGraphBuilder
) : RouteRegistry {
    private val registry = mutableMapOf<String, (Bundle) -> Route>()

    override fun register(
        route: String,
        navDeepLink: NavDeepLinkDslBuilder.() -> Unit,
        routeFactory: (Bundle) -> Route
    ) {
        navGraphBuilder.navigation(route = route, startDestination = "/") {
            deepLink(navDeepLink)
        }
        registry[route] = routeFactory
    }

    override fun buildRoute(route: String, args: Bundle?): Route? {
        return registry[route]?.invoke(args ?: Bundle.EMPTY)
    }
}
