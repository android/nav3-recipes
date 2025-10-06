package com.example.nav3recipes.passingarguments.viewmodels.koin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import org.koin.android.ext.koin.androidContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

/**
 * Passing navigation arguments to a Koin injected ViewModel
 *
 * - ViewModelStoreNavEntryDecorator ensures that ViewModels are scoped to the NavEntry
 */

data object RouteA
data class RouteB(val id: String)

class KoinViewModelsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        // The startKoin block should be placed in Application.onCreate.
        if (GlobalContext.getOrNull() == null) {
            GlobalContext.startKoin {
                androidContext(this@KoinViewModelsActivity)
                modules(
                    module {
                        viewModelOf(::RouteBViewModel)
                    }
                )
            }
        }

        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = remember { mutableStateListOf<Any>(RouteA) }

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },

                // In order to add the `ViewModelStoreNavEntryDecorator` (see comment below for why)
                // we also need to add the default `NavEntryDecorator`s as well. These provide
                // extra information to the entry's content to enable it to display correctly
                // and save its state.
                entryDecorators = listOf(
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<RouteA> {
                        ContentGreen("Welcome to Nav3") {
                            LazyColumn {
                                items(10) { i ->
                                    Button(onClick = {
                                        backStack.add(RouteB("$i"))
                                    }) {
                                        Text("$i")
                                    }
                                }
                            }
                        }
                    }
                    entry<RouteB> { key ->
                        val viewModel = koinViewModel<RouteBViewModel> {
                            parametersOf(key)
                        }
                        ScreenB(viewModel = viewModel)
                    }
                }
            )
        }
    }
}

@Composable
fun ScreenB(viewModel: RouteBViewModel) {
    ContentBlue("Route id: ${viewModel.navKey.id} ")
}

class RouteBViewModel(val navKey: RouteB) : ViewModel()