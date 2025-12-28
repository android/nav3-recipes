package com.example.nav3recipes.modular.koin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.compose.navigation3.getEntryProvider
import org.koin.androidx.scope.activityRetainedScope
import org.koin.core.Koin
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.component.KoinComponent
import org.koin.core.scope.Scope
import org.koin.dsl.koinApplication

/**
 * This recipe demonstrates how to use a modular approach with Navigation 3,
 * where different parts of the application are defined in separate modules and injected
 * into the main app using Koin.
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
@OptIn(KoinExperimentalAPI::class)
class KoinModularActivity : ComponentActivity(), AndroidScopeComponent, KoinComponent {
    // Local Koin Context Instance
    companion object {
        private val localKoin = koinApplication {
            modules(appModule)
        }.koin
    }
    // Override default Koin context to use the local one
    override fun getKoin(): Koin = localKoin
    override val scope : Scope by activityRetainedScope()
    val navigator: Navigator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setEdgeToEdgeConfig()
        setContent {
            Scaffold { paddingValues ->
                NavDisplay(
                    backStack = navigator.backStack,
                    modifier = Modifier.padding(paddingValues),
                    onBack = { navigator.goBack() },
                    entryProvider = getEntryProvider()
                )
            }
        }
    }

}
