package com.example.nav3recipes.genericoverlays

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import kotlinx.serialization.Serializable

@Serializable
private data object RouteA : NavKey

@Serializable
private data class RouteB(val id: String) : NavKey

@Serializable
private data class RouteC(val id: String) : NavKey

@Serializable
private data class RouteD(val id: String) : NavKey
class GenericOverlayActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberNavBackStack(RouteA)
            val overlaySceneStrategy = remember { OverlaySceneStrategy<NavKey>() }

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategy = overlaySceneStrategy,
                entryProvider = entryProvider {
                    entry<RouteA> {
                        ContentGreen("Welcome to Nav3") {
                            Button(onClick = { backStack.add(RouteB("123")) }) { Text("Click to open dialog") }
                            Button(onClick = { backStack.add(RouteC("1234")) }) { Text("Click to open alert dialog") }
                            Button(onClick = { backStack.add(RouteD("12345")) }) { Text("Click to open bottom sheet") }
                        }
                    }
                    entry<RouteB>(
                        metadata = OverlaySceneStrategy.overlay()
                    ) { key ->
                        Dialog(
                            onDismissRequest = { backStack.removeLastOrNull() },
                            properties = DialogProperties(windowTitle = key.id)
                        ) {
                            ContentBlue(
                                title = "Route id: ${key.id}",
                                modifier = Modifier.clip(
                                    shape = RoundedCornerShape(16.dp)
                                )
                            )
                        }
                    }
                    entry<RouteC>(
                        metadata = OverlaySceneStrategy.overlay()
                    ) { key ->
                        AlertDialog(
                            onDismissRequest = { backStack.removeLastOrNull() },
                            confirmButton = {
                                Button(onClick = { backStack.add(RouteD("123456")) }) { Text("Click to open bottom sheet") }
                            },
                            dismissButton = {
                                Button(onClick = { backStack.removeLastOrNull() }) { Text("Cancel") }
                            },
                            title = {
                                Text("Alert Dialog")
                            },
                            text = {
                                Text("This is an alert dialog, route id:${key.id} ")
                            }
                        )
                    }
                    entry<RouteD>(
                        metadata = OverlaySceneStrategy.overlay()
                    ) { key ->
                        ModalBottomSheet(
                            onDismissRequest = { backStack.removeLastOrNull() }
                        ) {
                            ContentBlue(
                                title = "Route id: ${key.id}",
                                modifier = Modifier.clip(
                                    shape = RoundedCornerShape(16.dp)
                                )
                            )
                        }
                    }
                }
            )
        }
    }
}