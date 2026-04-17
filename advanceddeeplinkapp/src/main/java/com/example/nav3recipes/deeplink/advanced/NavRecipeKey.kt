package com.example.nav3recipes.deeplink.advanced

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.nav3recipes.common.deeplink.User
import com.example.nav3recipes.deeplink.advanced.util.navigateUp
import kotlinx.serialization.Serializable

internal const val PATH_BASE = "https://www.nav3deeplink.com"

/**
 * Defines the NavKey used for this app.
 *
 * The keys are defined with this inheritance structure:
 * [NavDeepLinkRecipeKey] extends [NavRecipeKey] extends [NavKey].
 *
 * [NavKey] - the base Navigation 3 interface that can be used with [NavBackStack]
 *
 * [NavRecipeKey] - a sub-interface to supports member variables and functions
 * specific to this app
 *
 * [NavDeepLinkRecipeKey] - a sub-interface to ensure that all keys
 * that support deeplinking (or all keys that have children keys that can be deeplinked into)
 * implement these two fields:
 * 1. parent - the hierarchical parent of this key, required for building a synthetic backStack
 * 2. deeplinkUrl - the deeplink url associated with this key, required for supporting the
 * Up button (see [navigateUp] for more on this).
 */

internal interface NavRecipeKey: NavKey {
    val screenTitle: String
}

internal interface NavDeepLinkRecipeKey: NavRecipeKey {
    val parent: NavKey
    val deeplinkUrl: String
}

@Serializable
object Home: NavRecipeKey {
    override val screenTitle: String = "Home"
}

@Serializable
object Users: NavDeepLinkRecipeKey {
    override val screenTitle: String = "Users"
    override val parent: NavKey = Home
    override val deeplinkUrl: String
        get() = "$PATH_BASE/$DEEPLINK_URL_TAG_USERS"
}

@Serializable
internal data class UserDetail(
    val user: User
): NavDeepLinkRecipeKey {
    override val screenTitle: String = "User"
    override val parent: NavKey = Users
    override val deeplinkUrl: String
        get() = "$PATH_BASE/$DEEPLINK_URL_TAG_USER/${user.firstName}/${user.location}"
}

internal const val DEEPLINK_URL_TAG_USER = "user"
internal const val DEEPLINK_URL_TAG_USERS = "users"