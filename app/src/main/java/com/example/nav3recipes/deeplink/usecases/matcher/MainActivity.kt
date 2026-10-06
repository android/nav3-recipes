package com.example.nav3recipes.deeplink.usecases.matcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.invoke
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.common.deeplink.EntryScreen
import com.example.nav3recipes.common.deeplink.TextContent
import com.example.nav3recipes.deeplink.usecases.matcher.modules.HomeKey
import com.example.nav3recipes.deeplink.usecases.matcher.modules.ProfileKey
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
object FallbackKey : NavKey

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var deepLinkMatchers: Set<@JvmSuppressWildcards DeepLinkMatcher<NavKey, DeepLinkMatcher.MatchResult<NavKey>>>

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        val request = DeepLinkRequest(intent)
        val key = deepLinkMatchers.firstNotNullOfOrNull { matcher ->
            matcher.match(request)
        }?.key ?: FallbackKey

        setContent {
            val backStack: NavBackStack<NavKey> = rememberNavBackStack(key)
            NavDisplay(
                backStack = backStack,
                onBack = backStack::removeLastOrNull,
                entryProvider = entryProvider {
                    entry<HomeKey> { homeKey ->
                        EntryScreen("Home Screen") {
                            TextContent("Welcome, ${homeKey.name}!")
                        }
                    }
                    entry<ProfileKey> { profileKey ->
                        EntryScreen("Profile Screen") {
                            TextContent("User ID: ${profileKey.userId}")
                        }
                    }
                    entry<FallbackKey> {
                        EntryScreen("Fallback Key") {
                            TextContent(
                                "Failed to deep link - DeepLinkRequest " +
                                    "did not match with any DeepLinkMatcher"
                            )
                        }
                    }
                }
            )
        }
    }
}
