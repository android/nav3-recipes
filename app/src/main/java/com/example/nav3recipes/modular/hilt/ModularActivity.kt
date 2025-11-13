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

@AndroidEntryPoint
class ModularActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var entryProviderScopes: Set<@JvmSuppressWildcards EntryProviderInstaller>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null){
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
                        entryProviderScopes.forEach { builder -> this.builder() }
                    }
                )
            }
        }
    }
}
