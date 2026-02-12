package com.example.nav3recipes.interop

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.fragment.compose.AndroidFragment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable

@Serializable
private data object FragmentRoute : NavKey

@Serializable
private data class ViewRoute(val id: String) : NavKey

class InteropActivity : FragmentActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberNavBackStack(FragmentRoute)

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<FragmentRoute> {
                        Column(Modifier.fillMaxSize().wrapContentSize()) {
                            AndroidFragment<MyCustomFragment>()
                            Button(onClick = dropUnlessResumed {
                                backStack.add(ViewRoute("123"))
                            }) {
                                Text("Go to View")
                            }
                        }
                    }
                    entry<ViewRoute> { key ->
                        AndroidView(
                            modifier = Modifier.fillMaxSize().wrapContentSize(),
                            factory = { context ->
                                TextView(context).apply {
                                    text = "My View with key: ${key.id}"
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}