package com.example.nav3recipes.deeplink.usecases.deeplinkkey

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.dropUnlessResumed
import com.example.nav3recipes.common.deeplink.EntryScreen
import com.example.nav3recipes.common.deeplink.TextContent
import com.example.nav3recipes.ui.setEdgeToEdgeConfig

/**
 * Activity presenting a UI sandbox to test [DeepLinkKey] deep links.
 */
class DeepLinkKeyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            EntryScreen("DeepLinkKey Sandbox") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    var userIdText by remember { mutableStateOf("42") }
                    var categoryText by remember { mutableStateOf("electronics") }
                    var productIdText by remember { mutableStateOf("laptop-101") }

                    TextContent("Test launching deep links defined by DeepLinkKey implementations:")

                    // 1. Home Key Deep Link
                    ElevatedButton(
                        onClick = dropUnlessResumed {
                            launchDeepLink(HomeKey.uriPattern)
                        }
                    ) {
                        Text("Launch Home DeepLink\n(${HomeKey.uriPattern})")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. User Key Deep Link
                    OutlinedTextField(
                        value = userIdText,
                        onValueChange = { userIdText = it },
                        label = { Text("User ID") },
                        singleLine = true,
                    )
                    ElevatedButton(
                        onClick = dropUnlessResumed {
                            val id = userIdText.toIntOrNull() ?: 1
                            val uri = "https://www.nav3recipes.com/user/$id"
                            launchDeepLink(uri)
                        }
                    ) {
                        Text("Launch User DeepLink")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Product Key Deep Link
                    OutlinedTextField(
                        value = categoryText,
                        onValueChange = { categoryText = it },
                        label = { Text("Category") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = productIdText,
                        onValueChange = { productIdText = it },
                        label = { Text("Product ID") },
                        singleLine = true,
                    )
                    ElevatedButton(
                        onClick = dropUnlessResumed {
                            val uri = "https://www.nav3recipes.com/products/$categoryText/$productIdText"
                            launchDeepLink(uri)
                        }
                    ) {
                        Text("Launch Product DeepLink")
                    }
                }
            }
        }
    }

    private fun launchDeepLink(uriString: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            data = uriString.toUri()
        }
        startActivity(intent)
    }
}
