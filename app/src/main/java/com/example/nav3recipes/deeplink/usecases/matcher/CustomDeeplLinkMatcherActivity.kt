package com.example.nav3recipes.deeplink.usecases.matcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.example.nav3recipes.common.deeplink.EntryScreen
import com.example.nav3recipes.deeplink.usecases.matcher.modules.HomeDeepLinkKey
import com.example.nav3recipes.deeplink.usecases.matcher.modules.HomeKey
import com.example.nav3recipes.deeplink.usecases.matcher.modules.ProfileDeepLinkKey
import com.example.nav3recipes.deeplink.usecases.matcher.modules.ProfileKey
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.json.Json

class CustomDeepLinkMatcherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            EntryScreen {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DeepLinkInputSection(
                        placeholder = "Home name...",
                        buttonText = "JSON deep link to Home Module",
                        onButtonClick = { name ->
                            val intent = Intent(
                                this@CustomDeepLinkMatcherActivity,
                                MainActivity::class.java
                            )
                            val json = Json.encodeToString(
                                HomeKey.serializer(),
                                HomeKey(name)
                            )
                            intent.putExtra(HomeDeepLinkKey.toString(), json)
                            startActivity(intent)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DeepLinkInputSection(
                        placeholder = "Profile user ID...",
                        buttonText = "JSON deep link to Profile Module",
                        onButtonClick = { profileId ->
                            val intent = Intent(
                                this@CustomDeepLinkMatcherActivity,
                                MainActivity::class.java
                            )
                            val json = Json.encodeToString(
                                ProfileKey.serializer(),
                                ProfileKey(profileId)
                            )
                            intent.putExtra(ProfileDeepLinkKey.toString(), json)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeepLinkInputSection(
    placeholder: String,
    buttonText: String,
    onButtonClick: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        placeholder = { Text(placeholder, color = Color.Black.copy(alpha = 0.5f)) },
        value = text,
        singleLine = true,
        onValueChange = { text = it },
    )

    ElevatedButton(
        onClick =
            dropUnlessResumed {
                onButtonClick(text)
            }
    ) {
        Text(buttonText)
    }
}
