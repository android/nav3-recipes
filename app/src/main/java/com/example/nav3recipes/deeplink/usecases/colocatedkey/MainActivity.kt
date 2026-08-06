package com.example.nav3recipes.deeplink.usecases.colocatedkey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.invoke
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.common.deeplink.EntryScreen
import com.example.nav3recipes.common.deeplink.TextContent
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.serializer

class MainActivity : ComponentActivity() {

    // Register UriDeepLinkMatchers using the colocated URI patterns from each key
    private val deepLinkMatchers = listOf(
        UriDeepLinkMatcher(HomeKey.URI_PATTERN.toUri(), serializer<HomeKey>()),
        UriDeepLinkMatcher(UserKey.URI_PATTERN.toUri(), serializer<UserKey>()),
        UriDeepLinkMatcher(ProductKey.URI_PATTERN.toUri(), serializer<ProductKey>()),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        val request = DeepLinkRequest(intent)
        val matches = deepLinkMatchers.mapNotNull { it.match(request) }
        val bestMatch = matches.maxOrNull()
        val key = bestMatch?.key ?: FallbackKey

        setContent {
            val backStack: NavBackStack<NavKey> = rememberNavBackStack(key)
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<HomeKey> {
                        EntryScreen("Home Screen") {
                            TextContent("Welcome Home!\n(Matched: ${HomeKey.URI_PATTERN})")
                        }
                    }
                    entry<UserKey> { key ->
                        EntryScreen("User Profile") {
                            TextContent("User ID: ${key.id}\n(Matched: ${UserKey.URI_PATTERN})")
                        }
                    }
                    entry<ProductKey> { key ->
                        EntryScreen("Product Details") {
                            TextContent("Category: ${key.category}\nProduct ID: ${key.productId}\n(Matched: ${ProductKey.URI_PATTERN})")
                        }
                    }
                    entry<FallbackKey> {
                        EntryScreen("Fallback Destination") {
                            TextContent("No deep link pattern matched the incoming request.")
                        }
                    }
                }
            )
        }
    }
}
