package com.example.nav3recipes.deeplink.usecases.colocatedkey

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Interface for navigation keys that support deep linking.
 *
 * Colocating the URI pattern with the key definition keeps the URL template
 * and parameter structure together in a single place.
 */
interface DeepLinkKey : NavKey

@Serializable
data object HomeKey : DeepLinkKey {
    const val URI_PATTERN = "https://www.nav3recipes.com/home"
}

@Serializable
data class UserKey(
    val id: Int
) : DeepLinkKey {
    companion object {
        const val URI_PATTERN = "https://www.nav3recipes.com/user/{id}"
    }
}

@Serializable
data class ProductKey(
    val category: String,
    val productId: String
) : DeepLinkKey {
    companion object {
        const val URI_PATTERN = "https://www.nav3recipes.com/products/{category}/{productId}"
    }
}

@Serializable
data object FallbackKey : NavKey
