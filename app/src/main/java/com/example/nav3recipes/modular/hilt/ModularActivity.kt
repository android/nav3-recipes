package com.example.nav3recipes.modular.hilt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.entryProvider
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This recipe demonstrates how to use a modular approach with Navigation 3,
 * where different parts of the application are defined in separate modules and injected
 * into the main app using Dagger/Hilt.
 * 
 * Features (Conversation and Profile) are split into two modules: 
 * - api: defines the public facing routes for this feature
 * - impl: defines the entryProviders for this feature, these are injected into the app's main activity
 * The common module defines:
 * - a common navigator class that exposes a back stack and methods to modify that back stack
 * - a type that should be used by feature modules to inject entryProviders into the app's main activity
 * The app module creates the navigator by supplying a start destination and provides this navigator
 * to the rest of the app module (i.e. MainActivity) and the feature modules.
 */
@AndroidEntryPoint
class ModularActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add the start destination
        if (navigator.backStack.isEmpty()){
            navigator.goTo(ConversationList)
        }

        setEdgeToEdgeConfig()
        setContent {
            Scaffold { paddingValues ->
                NavDisplay(
                    backStack = navigator.backStack,
                    modifier = Modifier.padding(paddingValues),
                    onBack = { navigator.goBack() },
                    entryProvider = entryProvider {
                        entryProviderBuilders.forEach { builder -> this.builder() }
                    }
                )
            }
        }
    }
}
