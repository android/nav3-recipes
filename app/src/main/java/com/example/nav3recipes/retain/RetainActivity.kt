package com.example.nav3recipes.retain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.RetainedValuesStore
import androidx.compose.runtime.retain.RetainedValuesStoreRegistry
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.retain.retainRetainedValuesStoreRegistry
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig

private data object RouteA

private data class RouteB(val id: Int)

class RetainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = remember { mutableStateListOf<Any>(RouteA) }

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberRetainedValuesStoreNavEntryDecorator()
                ),
                entryProvider = { key ->
                    when (key) {
                        is RouteA -> NavEntry(key) {
                            ContentGreen("Nav3 with retain support") {
                                Text("Retained value: ${retainValue()}")
                                Button(onClick = dropUnlessResumed {
                                    backStack.add(RouteB(1))
                                }) {
                                    Text("Click to navigate")
                                }
                            }
                        }

                        is RouteB -> NavEntry(key) {
                            ContentBlue("Route id: ${key.id}") {
                                Text("Retained value: ${retainValue()}")
                                Button(onClick = dropUnlessResumed {
                                    backStack.add(RouteB(key.id + 1))
                                }) {
                                    Text("Click to navigate")
                                }
                            }
                        }

                        else -> {
                            error("Unknown route: $key")
                        }
                    }
                }
            )
        }
    }

    @Composable
    private fun retainValue() = retain { "Retained Value ${retainCount++}" }

    private companion object {
        var retainCount = 0
    }
}

/**
 * Returns a [RetainedValuesStoreNavEntryDecorator] that is remembered across recompositions backed
 * by [registry].
 *
 * The underlying storage is controlled by the provided [registry]. By default, a new
 * [RetainedValuesStoreRegistry] is retained at this point in the composition hierarchy and will be
 * destroyed when the composition is permanently discarded or when the returned decorator is removed
 * from the composition hierarchy. If you need the backing storage of this decorator to have a
 * different lifespan, you can manually manage and provide a [RetainedValuesStoreRegistry] with the
 * intended lifespan.
 *
 * @param registry The underlying [RetainedValuesStoreRegistry] used to provide
 *   [RetainedValuesStore] instances to [NavEntries][NavEntry]. This instance should be retained to
 *   properly survive destruction and recreation scenarios.
 */
@Composable
fun <T : Any> rememberRetainedValuesStoreNavEntryDecorator(
    registry: RetainedValuesStoreRegistry = retainRetainedValuesStoreRegistry()
): RetainedValuesStoreNavEntryDecorator<T> {
    val activity = LocalActivity.current
    return rememberRetainedValuesStoreNavEntryDecorator(registry) {
        activity?.isChangingConfigurations != true
    }
}

/**
 * Returns a [RetainedValuesStoreNavEntryDecorator] that is remembered across recompositions backed
 * by [registry].
 *
 * The underlying storage is controlled by the provided [registry]. By default, a new
 * [RetainedValuesStoreRegistry] is retained at this point in the composition hierarchy and will be
 * destroyed when the composition is permanently discarded or when the returned decorator is removed
 * from the composition hierarchy. If you need the backing storage of this decorator to have a
 * different lifespan, you can manually manage and provide a [RetainedValuesStoreRegistry] with the
 * intended lifespan.
 *
 * @param registry The underlying [RetainedValuesStoreRegistry] used to provide
 *   [RetainedValuesStore] instances to [NavEntries][NavEntry]. This instance should be retained to
 *   properly survive destruction and recreation scenarios.
 * @param removeRetainedValuesStoreOnPop A lambda that returns a Boolean indicating whether the
 *   [RetainedValuesStore] for a [NavEntry] should be removed when the [NavEntry] is popped from the
 *   back stack. If true, the entry's RetainedValuesStore will be removed, retiring all retained
 *   values for the removed content.
 */
@Composable
fun <T : Any> rememberRetainedValuesStoreNavEntryDecorator(
    registry: RetainedValuesStoreRegistry = retainRetainedValuesStoreRegistry(),
    removeRetainedValuesStoreOnPop: () -> Boolean,
): RetainedValuesStoreNavEntryDecorator<T> {
    val currentRemoveStoreOnPop = rememberUpdatedState(removeRetainedValuesStoreOnPop)
    return remember(registry) {
        RetainedValuesStoreNavEntryDecorator(registry) { currentRemoveStoreOnPop.value.invoke() }
    }
}

/**
 * Provides the content of each [NavEntry] with a dedicated [RetainedValuesStore] so that each nav
 * entry may retain its own values.
 *
 * @param registry The underlying [RetainedValuesStoreRegistry] used to provide
 *   [RetainedValuesStore] instances to [NavEntries][NavEntry]. This instance should be retained to
 *   properly survive destruction and recreation scenarios.
 * @param removeStoreOnPop A lambda that returns a Boolean indicating whether the
 *   [RetainedValuesStore] for a [NavEntry] should be removed when the [NavEntry] is popped from the
 *   back stack. If true, the entry's RetainedValuesStore will be removed, retiring all retained
 *   values for the removed content.
 */
class RetainedValuesStoreNavEntryDecorator<T : Any>(
    registry: RetainedValuesStoreRegistry,
    removeStoreOnPop: () -> Boolean,
) :
    NavEntryDecorator<T>(
        onPop = { key ->
            if (removeStoreOnPop()) {
                registry.clearChild(key)
            }
        },
        decorate = { entry ->
            registry.LocalRetainedValuesStoreProvider(entry.contentKey) { entry.Content() }
        },
    )
