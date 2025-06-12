package com.example.nav3recipes.returningresult

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nav3recipes.ui.theme.Nav3RecipesTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.runtime.navEntryDecorator
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.snapshotFlow // Corrected import
import kotlinx.serialization.Serializable
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer // Explicitly import Observer
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import kotlin.reflect.KClass // Corrected typo
import kotlin.reflect.full.cast // Added for cast extension function

// Define NavKeys for our destinations
@Serializable
private object Receiver : NavKey
@Serializable
private object SenderEvent : NavKey
@Serializable
private object SenderState : NavKey

// ViewModel to hold the SavedStateHandle for the current NavEntry
class ResultViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {
    // No need for a custom Factory if SavedStateHandle is provided automatically by viewModel()
}

// Custom ResultStore for event-based results, as hinted by the PDF
// This uses SavedStateHandle to persist the event across process death
private val LocalResultStore = compositionLocalOf<ResultStore> {
    error("No ResultStore provided")
}

class ResultStore(private val savedStateHandle: SavedStateHandle) {
    internal val EVENT_KEY = "nav_event_result"
    val _events = MutableSharedFlow<Any>(extraBufferCapacity = 1) // Changed to public
    val events: SharedFlow<Any> = _events.asSharedFlow() // Changed to public

    fun sendResult(result: Any) {
        savedStateHandle[EVENT_KEY] = result // Still use SavedStateHandle for persistence
        _events.tryEmit(result) // Emit to the flow for immediate consumption
    }
}

@Composable
fun <T : Any> ResultEffect(resultType: KClass<T>, onResult: (T) -> Unit) { // Added resultType: KClass<T>
    val resultStore = LocalResultStore.current
    LaunchedEffect(resultStore) {
        resultStore.events
            .filterNotNull()
            .collectLatest { result -> // Use collectLatest to handle only the latest event
                if (resultType.isInstance(result)) { // Use isInstance for type checking
                    onResult(resultType.cast(result)) // Cast to T
                }
            }
    }
}

@Composable
inline fun <reified T> rememberResult(key: String = "result", defaultValue: T? = null): MutableState<T?> {
    val resultViewModel: ResultViewModel = viewModel() // ViewModel will automatically get SavedStateHandle
    val savedStateHandle = resultViewModel.savedStateHandle

    val initialValue = remember { savedStateHandle.get<T>(key) ?: defaultValue }
    var result by rememberSaveable(key = key) { mutableStateOf(initialValue) }

    DisposableEffect(key, savedStateHandle) {
        val observer = androidx.lifecycle.Observer<T> { newValue ->
            result = newValue
        }
        savedStateHandle.getLiveData<T>(key).observeForever(observer)
        onDispose {
            savedStateHandle.getLiveData<T>(key).removeObserver(observer)
        }
    }
    return mutableStateOf(result)
}

class ReturningResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Nav3RecipesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val backStack = rememberNavBackStack(Receiver)

                    NavDisplay(
                        backStack = backStack,
                        onBack = { keysToRemove -> repeat(keysToRemove) { backStack.removeLastOrNull() } },
                        entryDecorators = listOf(
                            rememberSavedStateNavEntryDecorator(),
                            rememberSceneSetupNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(), // Needed for ViewModel scoping
                            navEntryDecorator { entry ->
                                // Provide ResultStore using the SavedStateHandle from the ViewModel scoped to this entry
                                val resultViewModel: ResultViewModel = viewModel() // ViewModel will automatically get SavedStateHandle
                                val resultStore = remember(resultViewModel) {
                                    ResultStore(resultViewModel.savedStateHandle)
                                }
                                CompositionLocalProvider(LocalResultStore provides resultStore) {
                                    entry.Content()
                                }
                            }
                        ),
                        entryProvider = entryProvider {
                            entry<Receiver> {
                                ReceiverScreen(
                                    onNavigateToSendEvent = { backStack.add(SenderEvent) },
                                    onNavigateToSendState = { backStack.add(SenderState) }
                                )
                            }
                            entry<SenderEvent> {
                                SenderEventScreen(
                                    onResultSent = { backStack.removeLastOrNull() }
                                )
                            }
                            entry<SenderState> {
                                SenderStateScreen(
                                    onResultSent = { backStack.removeLastOrNull() }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiverScreen(
    onNavigateToSendEvent: () -> Unit,
    onNavigateToSendState: () -> Unit
) {
    var eventResult by remember { mutableStateOf("No event result yet") }
    val stateResult by rememberResult<String?>("state_result_key", defaultValue = null)

    ResultEffect(String::class) { result -> // Pass String::class
        eventResult = "Event Result: $result"
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Receiver Screen", style = MaterialTheme.typography.headlineMedium)
        Text(text = eventResult, modifier = Modifier.padding(top = 8.dp))
        Text(text = "State Result: ${stateResult ?: "No state result yet"}", modifier = Modifier.padding(top = 8.dp))

        Button(
            onClick = onNavigateToSendEvent,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Go to Sender (Event)")
        }

        Button(
            onClick = onNavigateToSendState,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Go to Sender (State)")
        }
    }
}

@Composable
fun SenderEventScreen(
    onResultSent: () -> Unit
) {
    val resultStore = LocalResultStore.current
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Sender Event Screen", style = MaterialTheme.typography.headlineMedium)
        Button(
            onClick = {
                resultStore.sendResult("Hello from Event Sender!")
                onResultSent()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Send Event Result and Go Back")
        }
    }
}

@Composable
fun SenderStateScreen(
    onResultSent: () -> Unit
) {
    val resultViewModel: ResultViewModel = viewModel()
    val savedStateHandle = resultViewModel.savedStateHandle

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Sender State Screen", style = MaterialTheme.typography.headlineMedium)
        Button(
            onClick = {
                savedStateHandle["state_result_key"] = "Hello from State Sender!"
                onResultSent()
            },
            modifier = Modifier.padding(top = 16.dp) // Fixed syntax error here
        ) {
            Text("Send State Result and Go Back")
        }
    }
}
