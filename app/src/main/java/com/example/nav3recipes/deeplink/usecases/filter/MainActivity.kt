package com.example.nav3recipes.deeplink.usecases.filter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.actionFilter
import androidx.navigation3.runtime.deeplink.invoke
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.common.deeplink.EntryScreen
import com.example.nav3recipes.common.deeplink.TextContent
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

internal const val VIEWER_URI_PATTERN = "https://www.nav3recipes.com/viewer?title={title}"

@Serializable
internal data class ViewerKey(val title: String) : NavKey

@Serializable
internal data object FallbackKey : NavKey

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        val request = DeepLinkRequest(intent)
        val compositeFilter =
            DeepLinkMatcher.actionFilter(Intent.ACTION_VIEW) and
                    (DeepLinkMatcher.mimeTypeFilter("image/png") or
                            DeepLinkMatcher.mimeTypeFilter("image/jpeg"))
        val deepLinkMatcher = UriDeepLinkMatcher(
            VIEWER_URI_PATTERN.toUri(),
            serializer<ViewerKey>(),
            filters = listOf(compositeFilter)
        )

        val matchResult = deepLinkMatcher.match(request)
        val key = matchResult?.key ?: FallbackKey

        setContent {
            val backStack: NavBackStack<NavKey> = rememberNavBackStack(key)
            NavDisplay(
                backStack = backStack,
                onBack = backStack::removeLastOrNull,
                entryProvider = entryProvider {
                    entry<ViewerKey> { key ->
                        EntryScreen("Viewer") {
                            TextContent(
                                "Matched composite filter!\n\n" +
                                    "Title: ${key.title}\n" +
                                    "Action: ${intent.action}\n" +
                                    "MIME Type: ${intent.type}"
                            )
                        }
                    }
                    entry<FallbackKey> {
                        EntryScreen("Fallback Key") {
                            TextContent(
                                "Failed to deep link - Request did not satisfy composite filter!\n\n" +
                                    "Required Filter:\n" +
                                    "ACTION_VIEW and (image/png or image/jpeg)\n\n" +
                                    "Received:\n" +
                                    "Action: ${intent.action ?: "none"}\n" +
                                    "MIME Type: ${intent.type ?: "none"}"
                            )
                        }
                    }
                }
            )
        }
    }
}
