package com.example.nav3recipes.deeplink.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher.MatchResult
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.invoke
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.common.deeplink.EntryScreen
import com.example.nav3recipes.common.deeplink.FriendsList
import com.example.nav3recipes.common.deeplink.LIST_USERS
import com.example.nav3recipes.common.deeplink.TextContent
import com.example.nav3recipes.deeplink.basic.ui.URL_HOME_EXACT
import com.example.nav3recipes.deeplink.basic.ui.URL_SEARCH
import com.example.nav3recipes.deeplink.basic.ui.URL_USERS_WITH_FILTER
import com.example.nav3recipes.ui.setEdgeToEdgeConfig

/**
 * Parses a target deeplink into a NavKey. There are several crucial steps involved:
 *
 * STEP 1. Parse supported deeplinks (URLs that can be deeplinked into) into a readily readable
 *  format (see [DeepLinkUri])
 * STEP 2. Parse the requested deeplink into a readily readable format (see [DeepLinkRequest])
 *  **note** the parsed requested deeplink and parsed supported deeplinks should be cohesive with each
 *  other to facilitate comparison and finding a match
 * STEP 3. Compare the requested deeplink target with supported deeplinks in order to find a match
 *  (see [MatchResult]). The match result's format should enable conversion from result
 *  to backstack key, regardless of what the conversion method may be.
 * STEP 4. Associate the match results with the correct backstack key
 *
 * This recipes provides an example for each of the above steps by way of kotlinx.serialization.
 *
 * **This recipe is designed to focus on parsing an intent into a key, and therefore these additional
 * deeplink considerations are not included in this scope**
 *  - Create synthetic backStack
 *  - Multi-modular setup
 *  - DI
 *  - Managing TaskStack
 *  - Up button ves Back Button
 *
 */
class MainActivity : ComponentActivity() {
    /** STEP 1. Parse supported deeplinks */
    // internal so that landing activity can link to this in the kdocs
    internal val deepLinkMatchers: List<DeepLinkMatcher<out NavKey>> = listOf(
        // "https://www.nav3recipes.com/home"
        UriDeepLinkMatcher(DeepLinkUri(URL_HOME_EXACT), HomeKey.serializer()),
        // "https://www.nav3recipes.com/users/with/{filter}"
        UriDeepLinkMatcher(DeepLinkUri(URL_USERS_WITH_FILTER), UsersKey.serializer()),
        // "https://www.nav3recipes.com/users/search?{firstName}&{age}&{location}"
        UriDeepLinkMatcher(DeepLinkUri(URL_SEARCH), SearchKey.serializer()),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        val key: NavKey = intent.data?.let {
            val request = DeepLinkRequest(intent)

            deepLinkMatchers
                .mapNotNull { it.match(request) }
                .maxOrNull()
                ?.key
        } ?: HomeKey // fallback if intent.uri is null or match is not found

        /**
         * Then pass starting key to backstack
         */
        setContent {
            val backStack: NavBackStack<NavKey> = rememberNavBackStack(key)
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<HomeKey> { key ->
                        EntryScreen(key.name) {
                            TextContent("<matches exact url>")
                        }
                    }
                    entry<UsersKey> { key ->
                        EntryScreen("${key.name} : ${key.filter}") {
                            TextContent("<matches path argument>")
                            val list = when {
                                key.filter.isEmpty() -> LIST_USERS
                                key.filter == UsersKey.FILTER_OPTION_ALL -> LIST_USERS
                                else -> LIST_USERS.take(5)
                            }
                            FriendsList(list)
                        }
                    }
                    entry<SearchKey> { search ->
                        EntryScreen(search.name) {
                            TextContent("<matches query parameters, if any>")
                            val matchingUsers = LIST_USERS.filter { user ->
                                (search.firstName == null || user.firstName == search.firstName) &&
                                        (search.location == null || user.location == search.location) &&
                                        (search.ageMin == null || user.age >= search.ageMin) &&
                                        (search.ageMax == null || user.age <= search.ageMax)
                            }
                            FriendsList(matchingUsers)
                        }
                    }
                }
            )
        }
    }
}