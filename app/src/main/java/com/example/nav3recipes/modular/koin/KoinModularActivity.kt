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
import org.koin.android.ext.koin.androidContext
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.compose.navigation3.getEntryProvider
import org.koin.androidx.scope.activityRetainedScope
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatform

/**
 * This recipe demonstrates how to use a modular approach with Navigation 3,
 * where different parts of the application are defined in separate modules and injected
 * into the main app using Koin.
 * 
 * Features (Conversation and Profile) are defined in their own Koin modules:
 * - `ConversationModule` and `ProfileModule` declare navigation entries for their screens.
 *
 * A shared `Navigator` class manages the backstack.
 *
 * The `appModule` includes the feature modules, creates the `Navigator` with a start destination,
 * and makes it available for injection into the `KoinModularActivity` and feature modules.
 *
 */
@OptIn(KoinExperimentalAPI::class)
class KoinModularActivity : ComponentActivity(), AndroidScopeComponent {

    override val scope : Scope by activityRetainedScope()
    val navigator: Navigator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //prevent any already launched Koin instance with other config
        if (KoinPlatform.getKoinOrNull() != null) {
            stopKoin()
        }
        // The startKoin block should be placed in Application.onCreate.
        startKoin {
            androidContext(this@KoinModularActivity)
            modules(appModule)
        }

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
