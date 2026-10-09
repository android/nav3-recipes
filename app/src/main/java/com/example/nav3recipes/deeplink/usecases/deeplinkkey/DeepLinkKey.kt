package com.example.nav3recipes.deeplink.usecases.deeplinkkey

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Interface for navigation keys that support deep linking.
 *
 * Associating the URI pattern with the key definition keeps the URL template
 * and parameter structure together in a single place.
 */
interface DeepLinkKey {
    val uriPattern: String
}

@Serializable
data object HomeKey : DeepLinkKey, NavKey {
    override val uriPattern: String = "https://www.nav3recipes.com/home"
}

@Serializable
data class UserKey(
    val id: Int
) : NavKey {
    companion object : DeepLinkKey {
        override val uriPattern: String = "https://www.nav3recipes.com/user/{id}"
    }
}

@Serializable
data class ProductKey(
    val category: String,
    val productId: String
) : NavKey {
    companion object : DeepLinkKey {
        override val uriPattern: String = "https://www.nav3recipes.com/products/{category}/{productId}"
    }
}

@Serializable
data object FallbackKey : NavKey
