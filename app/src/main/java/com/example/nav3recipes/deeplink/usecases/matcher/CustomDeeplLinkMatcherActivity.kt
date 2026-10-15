package com.example.nav3recipes.deeplink.usecases.matcher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.RequestExtrasKey
import com.example.nav3recipes.common.deeplink.EntryScreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class CustomDeepLinkMatcherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            EntryScreen {
                Column(
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    var text by remember { mutableStateOf("") }
                    OutlinedTextField(
                        placeholder = { Text("Your name...", color = Color.Black.copy(alpha = 0.5f)) },
                        value = text,
                        singleLine = true,
                        onValueChange = { text = it },
                    )

                    ElevatedButton(
                        onClick =
                            dropUnlessResumed {
                                val intent = Intent(
                                    this@CustomDeepLinkMatcherActivity,
                                    MainActivity::class.java
                                )
                                val json = Json.encodeToString(HomeKey.serializer(), HomeKey(text))
                                intent.putExtra(JsonDeepLinkMatcherKey.toString(), json)
                                startActivity(intent)
                        }
                    ) {
                        Text("Sign up")
                    }
                }
            }
        }
    }
}

internal data object JsonDeepLinkMatcherKey: RequestExtrasKey<String>

internal class JsonDeepLinkMatcher<T: NavKey>(val serializer: KSerializer<T>): DeepLinkMatcher<T, DeepLinkMatcher.MatchResult<T>>() {
    override fun matchRequest(request: DeepLinkRequest): MatchResult<T>? {
        val json = request.extras[JsonDeepLinkMatcherKey] ?: return null
        return try {
            val result = Json.decodeFromString(serializer, json)
            MatchResult(result)
        } catch (e: SerializationException) {
            Log.v("DeepLinkMatcher", "Failed to decode json", e)
            return null
        }
    }
}
