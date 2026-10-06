package com.example.nav3recipes.deeplink.usecases.matcher

import android.util.Log
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.RequestExtrasKey
import androidx.navigation3.runtime.deeplink.get
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal class JsonDeepLinkMatcher<T : NavKey>(
    private val extrasKey: RequestExtrasKey<String>,
    private val serializer: KSerializer<T>,
) : DeepLinkMatcher<NavKey, DeepLinkMatcher.MatchResult<NavKey>>() {

    override fun matchRequest(request: DeepLinkRequest): MatchResult<NavKey>? {
        val json = request.extras[extrasKey] ?: return null
        return try {
            val result = Json.decodeFromString(serializer, json)
            MatchResult(result)
        } catch (e: SerializationException) {
            Log.v(TAG, "Failed to decode json", e)
            null
        }
    }

    private companion object {
        const val TAG = "DeepLinkMatcher"
    }
}
