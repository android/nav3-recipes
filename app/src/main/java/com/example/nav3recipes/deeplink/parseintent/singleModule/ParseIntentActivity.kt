package com.example.nav3recipes.deeplink.parseintent.singleModule

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

/**
 * Parses a target deeplink into a NavKey. There are several crucial steps involved:
 *
 * STEP 1.Parse supported deeplinks (URLs that can be deeplinked into) into a readily readable
 *  format (see [DeepLinkMapper])
 * STEP 2. Parse the requested deeplink into a readily readable, format (see [DeepLinkRequest])
 *  **note** the parsed requested deeplink and parsed supported deeplinks should be cohesive with each
 *  other to facilitate comparison and finding a match
 * STEP 3. Compare the requested deeplink target with supported deeplinks in order to find a match
 *  (see [DeepLinkMatchResult]). The match result's format should enable conversion from result
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

@Serializable
internal object HomeKey: DeepLink, NavKey {
    override val name: String = STRING_LITERAL_HOME
}

@Serializable
internal data class UsersKey(
    val filter: String,
): NavKey {
    companion object : DeepLink {
        const val FILTER_KEY = STRING_LITERAL_FILTER
        const val FILTER_OPTION_RECENTLY_ADDED = "recentlyAdded"
        const val FILTER_OPTION_ALL = "all"

        override val name: String = STRING_LITERAL_USERS
    }
}

@Serializable
internal data class SearchKey(
    val firstName: String? = null,
    val ageMin: Int? = null,
    val ageMax: Int? = null,
    val location: String? = null,
): NavKey {
    companion object : DeepLink {
        override val name: String = STRING_LITERAL_SEARCH
    }
}

@Serializable
internal data class User(
    val firstName: String,
    val age: Int,
    val location: String,
)


class ParseIntentActivity : ComponentActivity() {
    /** STEP 1. Parse supported deeplinks */
    private val deepLinkMapperCandidates: List<DeepLinkMapper<out NavKey>> = listOf(
        // "https://www.nav3recipes.com/home"
        DeepLinkMapper(HomeKey.serializer(), (URL_HOME_EXACT).toUri()),
        // "https://www.nav3recipes.com/users/with/{filter}"
        DeepLinkMapper(UsersKey.serializer(), (URL_USERS_WITH_FILTER).toUri()),
        // "https://www.nav3recipes.com/users/search?{firstName}&{age}&{location}"
        DeepLinkMapper(SearchKey.serializer(), (URL_SEARCH.toUri())),
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve the target Uri
        val uri: Uri? = intent.data
        // associate the target with the correct backstack key
        val startingKey: NavKey = uri?.let {
            /** STEP 2. Parse requested deeplink */
            val target = DeepLinkRequest(uri)
            /** STEP 3. Compared requested with supported deeplink to find match*/
            val match = deepLinkMapperCandidates.firstNotNullOfOrNull { candidate ->
                target.match(candidate)
            }
            /** STEP 4. If match is found, associate match to the correct key*/
            match?.let {
                   //leverage kotlinx.serialization's Decoder to decode
                   // match result into a backstack key
                    KeyDecoder(match.args)
                        .decodeSerializableValue(match.serializer)
            }
        } ?: HomeKey

        /**
         * Then pass starting key to backstack
         */
        setContent {
            val backStack: NavBackStack<NavKey> = rememberNavBackStack(startingKey)
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<HomeKey> { key ->
                        EntryScreen("Home") {
                            TextContent("<matches exact url>")
                        }
                    }
                    entry<UsersKey> { key ->
                        EntryScreen("Users : ${key.filter}") {
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
                        EntryScreen("Search") {
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