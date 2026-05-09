package com.example.nav3recipes.passingarguments.viewmodels.metro

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.modular.metro.ActivityScope
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.android.ActivityKey
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.serialization.Serializable

@Serializable
data object RouteA

@Serializable
data class RouteB(val id: String)

@ContributesIntoMap(ActivityScope::class, binding<Activity>())
@ActivityKey
@Inject
class MetroViewModelsActivity(private val metroVmf: MetroViewModelFactory) : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberSaveable { mutableStateListOf<Any>(RouteA) }


            CompositionLocalProvider(LocalMetroViewModelFactory provides metroVmf) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },

                    // In order to add the `ViewModelStoreNavEntryDecorator` (see comment below for why)
                    // we also need to add the default `NavEntryDecorator`s as well. These provide
                    // extra information to the entry's content to enable it to display correctly
                    // and save its state.
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<RouteA> {
                            ContentGreen("Welcome to Nav3") {
                                LazyColumn {
                                    items(10) { i ->
                                        Button(onClick = dropUnlessResumed {
                                            backStack.add(RouteB("$i"))
                                        }) {
                                            Text("$i")
                                        }
                                    }
                                }
                            }
                        }
                        entry<RouteB> { key ->
                            val viewModel: RouteBViewModel = assistedMetroViewModel<RouteBViewModel, RouteBViewModel.Factory> {
                                create(key)
                            }
                            ScreenB(viewModel = viewModel)
                        }
                    }
                )
            }
        }

    }
}

@Composable
fun ScreenB(viewModel: RouteBViewModel) {
    ContentBlue("Route id: ${viewModel.navKey.id} ")
}

@AssistedInject
class RouteBViewModel (
    @Assisted val navKey: RouteB
) : ViewModel() {

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(ActivityScope::class)
    interface Factory: ManualViewModelAssistedFactory {
        fun create(@Assisted navKey: RouteB): RouteBViewModel
    }
}
