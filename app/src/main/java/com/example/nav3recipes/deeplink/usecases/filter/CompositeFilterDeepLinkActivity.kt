package com.example.nav3recipes.deeplink.usecases.filter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.common.deeplink.EntryScreen
import com.example.nav3recipes.common.deeplink.PaddedButton
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable

private const val DEMO_URI = "https://www.nav3recipes.com/viewer?title=SamplePhoto"

@Serializable
private data object FilterHomeKey : NavKey

class CompositeFilterDeepLinkActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            EntryScreen {
                val backStack = rememberNavBackStack(FilterHomeKey)
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<FilterHomeKey> {
                            CompositeFilterScreen(
                                onLaunch = { action, mimeType ->
                                    val uri = DEMO_URI.toUri()
                                    val intent = Intent(
                                        this@CompositeFilterDeepLinkActivity,
                                        MainActivity::class.java
                                    ).apply {
                                        this.action = action
                                        if (mimeType != null) {
                                            setDataAndType(uri, mimeType)
                                        } else {
                                            data = uri
                                        }
                                    }
                                    startActivity(intent)
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CompositeFilterScreen(onLaunch: (action: String, mimeType: String?) -> Unit) {
    var selectedAction by remember { mutableStateOf(Intent.ACTION_VIEW) }
    var selectedMimeType by remember { mutableStateOf<String?>("image/png") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Composite Filter Rule:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                val ruleText = buildAnnotatedString {
                    append("ACTION_VIEW ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("AND")
                    }
                    append(" (image/png ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("OR")
                    }
                    append(" image/jpeg)")
                }
                Text(
                    text = ruleText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Intent Action:", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Intent.ACTION_VIEW to "ACTION_VIEW",
                Intent.ACTION_SEND to "ACTION_SEND"
            ).forEach { (action, label) ->
                FilterChip(
                    selected = selectedAction == action,
                    onClick = { selectedAction = action },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("MIME Type:", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "image/png" to "PNG",
                "image/jpeg" to "JPEG",
                "application/pdf" to "PDF",
                null to "None"
            ).forEach { (mime, label) ->
                FilterChip(
                    selected = selectedMimeType == mime,
                    onClick = { selectedMimeType = mime },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val willMatch = selectedAction == Intent.ACTION_VIEW &&
            (selectedMimeType == "image/png" || selectedMimeType == "image/jpeg")

        Text(
            text = if (willMatch) "Result: Will Match (Viewer)" else "Result: Will Fail (Fallback)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (willMatch) Color(0xFF2E7D32) else Color(0xFFC62828)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PaddedButton(
            text = "Test Deep Link",
            onClick = dropUnlessResumed { onLaunch(selectedAction, selectedMimeType) }
        )
    }
}
