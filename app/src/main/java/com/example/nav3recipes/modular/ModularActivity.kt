package com.example.nav3recipes.modular

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entryProvider
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This recipe demonstrates how to use a modular approach with Navigation 3,
 * where different parts of the application are defined in separate modules.
 * 
 * Features (Conversation and Profile) are split into two modules: 
 * - api: defines the public facing routes for this feature
 * - impl: defines the entryProviders for this feature, these are injected into the app's main activity
 * The common module defines how the back stack should be created.
 * The app module creates the back stack by supplying a start destination and provides this back stack to the rest of the app module (i.e. MainActivity) and the feature modules. 
 */
@AndroidEntryPoint
class ModularActivity : ComponentActivity() {

    @Inject
    lateinit var backStack: SnapshotStateList<Any>

    @Inject
    lateinit var entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderBuilder<Any>.() -> Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEdgeToEdgeConfig()
        setContent {
            Scaffold { paddingValues ->
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.padding(paddingValues),
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entryProviderBuilders.forEach { builder -> this.builder() }
                    }
                )
            }
        }
    }
}
