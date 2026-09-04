package com.example.nav3recipes.deeplink.usecases.filter

import androidx.navigation3.runtime.deeplink.DeepLinkMatcher.Filter

/**
 * Combines this [Filter] with [other] using logical AND.
 *
 * Short-circuits evaluation if this filter returns `false`.
 *
 * **Example:**
 * ```kotlin
 * val compositeFilter = actionFilter(Intent.ACTION_VIEW) and mimeTypeFilter("image/png")
 * ```
 */
infix fun Filter.and(other: Filter): Filter = Filter { request ->
    filterRequest(request) && other.filterRequest(request)
}

/**
 * Combines this [Filter] with [other] using logical OR.
 *
 * Short-circuits evaluation if this filter returns `true`.
 *
 * **Example:**
 * ```kotlin
 * val imageFilter = mimeTypeFilter("image/png") or mimeTypeFilter("image/jpeg")
 * ```
 */
infix fun Filter.or(other: Filter): Filter = Filter { request ->
    filterRequest(request) || other.filterRequest(request)
}

/**
 * Inverts the result of this [Filter] using logical NOT.
 *
 * **Example:**
 * ```kotlin
 * val nonPdfFilter = !mimeTypeFilter("application/pdf")
 * ```
 */
operator fun Filter.not(): Filter = Filter { request ->
    !filterRequest(request)
}

