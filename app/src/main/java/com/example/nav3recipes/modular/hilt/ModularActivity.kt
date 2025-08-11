package com.example.nav3recipes.modular.hilt

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.content.ContentYellow
import com.example.nav3recipes.conversation.ConversationDetailFragmentScreen
import com.example.nav3recipes.conversation.ConversationDetailScreen
import com.example.nav3recipes.conversation.ConversationId
import com.example.nav3recipes.conversation.ConversationListScreen
import com.example.nav3recipes.profile.ProfileScreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

// Serializable navigation entries
@Serializable
sealed interface NavigationEntry

// Anonymous user routes
@Serializable
data object Welcome : NavigationEntry

@Serializable
data object Login : NavigationEntry

@Serializable
data object Register : NavigationEntry

@Serializable
data object ForgotPassword : NavigationEntry

// Authenticated user routes - main tabs
@Serializable
sealed interface AuthenticatedTab : NavigationEntry {
    val icon: ImageVector
        get() = when (this) {
            is ConversationTab -> Icons.Default.Face
            is MyProfileTab -> Icons.Default.Person
            is SettingsTab -> Icons.Default.Settings
        }
}

@Serializable
data object ConversationTab : AuthenticatedTab

@Serializable
data object MyProfileTab : AuthenticatedTab

@Serializable
data object SettingsTab : AuthenticatedTab

// Conversation sub-routes
@Serializable
data class ConversationDetail(val id: Int) : NavigationEntry

@Serializable
data class ConversationDetailFragment(val id: Int) : NavigationEntry

@Serializable
data object UserProfile : NavigationEntry

@Serializable
sealed class NavigationState {
    @Serializable
    data class Anonymous(
        val backStack: List<NavigationEntry> = listOf(Welcome)
    ) : NavigationState()

    @Serializable
    data class Authenticated(
        val currentTab: AuthenticatedTab = ConversationTab,
        val backStack: List<NavigationEntry> = listOf(ConversationTab)
    ) : NavigationState()
}

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val TAG = "NavigationViewModel"
        private val AUTHENTICATED_TABS: List<AuthenticatedTab> = listOf(
            ConversationTab, MyProfileTab, SettingsTab
        )
    }

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Anonymous())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    private val tabStacks = mutableMapOf<AuthenticatedTab, MutableList<NavigationEntry>>()

    init {
        // Initialize tab stacks
        AUTHENTICATED_TABS.forEach { tab ->
            tabStacks[tab] = mutableListOf(tab)
        }
    }

    fun authenticate() {
        viewModelScope.launch {
            Log.d(TAG, "User authenticated")
            _navigationState.value = NavigationState.Authenticated(
                currentTab = ConversationTab,
                backStack = tabStacks[ConversationTab]?.toList() ?: listOf(ConversationTab)
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "User logged out")
            // Reset tab stacks
            AUTHENTICATED_TABS.forEach { tab ->
                tabStacks[tab] = mutableListOf(tab)
            }
            _navigationState.value = NavigationState.Anonymous()
        }
    }

    fun navigateToTab(tab: AuthenticatedTab) {
        viewModelScope.launch {
            val currentState = _navigationState.value
            if (currentState is NavigationState.Authenticated) {
                Log.d(TAG, "Switching to tab: $tab")
                _navigationState.value = currentState.copy(
                    currentTab = tab,
                    backStack = tabStacks[tab]?.toList() ?: listOf(tab)
                )
            }
        }
    }

    fun navigateToEntry(entry: NavigationEntry) {
        viewModelScope.launch {
            val currentState = _navigationState.value

            when (currentState) {
                is NavigationState.Authenticated -> {
                    // Add to current tab's stack
                    val currentTabStack =
                        tabStacks[currentState.currentTab] ?: mutableListOf(currentState.currentTab)
                    val newStack = currentTabStack.toMutableList()
                    newStack.add(entry)
                    tabStacks[currentState.currentTab] = newStack

                    Log.d(TAG, "Adding $entry to tab ${currentState.currentTab}")
                    _navigationState.value = currentState.copy(backStack = newStack)
                }

                is NavigationState.Anonymous -> {
                    // Add to anonymous stack
                    val newStack = currentState.backStack.toMutableList()
                    newStack.add(entry)
                    Log.d(TAG, "Adding $entry to anonymous stack")
                    _navigationState.value = currentState.copy(backStack = newStack)
                }
            }
        }
    }

    fun navigateBack(): Boolean {
        val currentState = _navigationState.value

        return when (currentState) {
            is NavigationState.Authenticated -> {
                val currentTabStack = tabStacks[currentState.currentTab] ?: mutableListOf()
                if (currentTabStack.size > 1) {
                    currentTabStack.removeLastOrNull()
                    Log.d(
                        TAG,
                        "Removing from tab ${currentState.currentTab}, remaining: $currentTabStack"
                    )
                    _navigationState.value = currentState.copy(
                        backStack = currentTabStack.toList()
                    )
                    true
                } else {
                    false
                }
            }
            is NavigationState.Anonymous -> {
                if (currentState.backStack.size > 1) {
                    val newStack = currentState.backStack.dropLast(1)
                    Log.d(TAG, "Removing from anonymous stack, remaining: $newStack")
                    _navigationState.value = currentState.copy(backStack = newStack)
                    true
                } else {
                    false
                }
            }
        }
    }
}

@AndroidEntryPoint
class ModularActivity : FragmentActivity() {

    companion object {
        private const val TAG = "ModularActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: Initializing ModularActivity")
        super.onCreate(savedInstanceState)
        setEdgeToEdgeConfig()
        setContent {
            val viewModel: NavigationViewModel = hiltViewModel()
            val navigationState by viewModel.navigationState.collectAsState()

            when (val state = navigationState) {
                is NavigationState.Authenticated -> {
                    Log.d(TAG, "Showing authenticated UI")
                    AuthenticatedUI(
                        navigationState = state,
                        onTabSelected = viewModel::navigateToTab,
                        onNavigate = viewModel::navigateToEntry,
                        onBack = { viewModel.navigateBack() },
                        onLogout = viewModel::logout
                    )
                }

                is NavigationState.Anonymous -> {
                    Log.d(TAG, "Showing anonymous UI")
                    AnonymousUI(
                        navigationState = state,
                        onNavigate = viewModel::navigateToEntry,
                        onBack = { viewModel.navigateBack() },
                        onAuthenticate = viewModel::authenticate
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedUI(
    navigationState: NavigationState.Authenticated,
    onTabSelected: (AuthenticatedTab) -> Unit,
    onNavigate: (NavigationEntry) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val authenticatedTabs = listOf(ConversationTab, MyProfileTab, SettingsTab)

    Scaffold(
        bottomBar = {
            NavigationBar {
                authenticatedTabs.forEach { tab ->
                    val isSelected = tab == navigationState.currentTab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            Log.d("ModularActivity", "Tab clicked: $tab")
                            onTabSelected(tab)
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    ) { contentPadding ->
        NavDisplay(
            backStack = navigationState.backStack,
            onBack = {
                Log.d("ModularActivity", "Back pressed in authenticated UI")
                onBack()
            },
            entryProvider = entryProvider {
                entry<ConversationTab> {
                    ConversationListScreen(
                        onConversationClicked = { conversationId ->
                            Log.d("ModularActivity", "Conversation clicked: $conversationId")
                            onNavigate(ConversationDetail(conversationId.value))
                        },
                        onConversationFragmentClicked = { conversationId ->
                            Log.d(
                                "ModularActivity",
                                "Conversation fragment clicked: $conversationId"
                            )
                            onNavigate(ConversationDetailFragment(conversationId.value))
                        },
                    )
                }

                entry<ConversationDetail> { key ->
                    ConversationDetailScreen(
                        conversationId = ConversationId(key.id),
                        onProfileClicked = {
                            Log.d("ModularActivity", "Profile clicked from conversation detail")
                            onNavigate(UserProfile)
                        }
                    )
                }

                entry<ConversationDetailFragment> { key ->
                    ConversationDetailFragmentScreen(
                        conversationId = ConversationId(key.id),
                        onProfileClicked = {
                            Log.d("ModularActivity", "Profile clicked from conversation fragment")
                            onNavigate(UserProfile)
                        }
                    )
                }

                entry<MyProfileTab> {
                    ContentPurple("My Profile") {
                        Text("My Profile")
                    }
                }

                entry<UserProfile> {
                    ProfileScreen()
                }

                entry<SettingsTab> {
                    SettingsScreen(
                        onLogout = {
                            Log.d("ModularActivity", "User logged out")
                            onLogout()
                        }
                    )
                }
            },
            modifier = Modifier.padding(contentPadding)
        )
    }
}

@Composable
private fun AnonymousUI(
    navigationState: NavigationState.Anonymous,
    onNavigate: (NavigationEntry) -> Unit,
    onBack: () -> Unit,
    onAuthenticate: () -> Unit
) {
    NavDisplay(
        backStack = navigationState.backStack,
        onBack = {
            Log.d("ModularActivity", "Back pressed in anonymous UI")
            onBack()
        },
        entryProvider = entryProvider {
            entry<Welcome> {
                ContentGreen("Welcome to Nav3 Recipes") {
                    Column {
                        Button(onClick = {
                            Log.d("ModularActivity", "Login button clicked")
                            onNavigate(Login)
                        }) {
                            Text("Login")
                        }
                        Button(onClick = {
                            Log.d("ModularActivity", "Register button clicked")
                            onNavigate(Register)
                        }) {
                            Text("Register")
                        }
                    }
                }
            }

            entry<Login> {
                ContentBlue("Login Screen") {
                    Column {
                        Button(onClick = {
                            Log.d("ModularActivity", "User authenticated")
                            onAuthenticate()
                        }) {
                            Text("Sign In")
                        }
                        Button(onClick = {
                            Log.d("ModularActivity", "Navigate to register")
                            onNavigate(Register)
                        }) {
                            Text("Don't have account? Register")
                        }
                        Button(onClick = {
                            Log.d("ModularActivity", "Navigate to forgot password")
                            onNavigate(ForgotPassword)
                        }) {
                            Text("Forgot Password?")
                        }
                    }
                }
            }

            entry<Register> {
                ContentYellow("Register Screen") {
                    Column {
                        Button(onClick = {
                            Log.d("ModularActivity", "Registration completed")
                            onAuthenticate()
                        }) {
                            Text("Create Account")
                        }
                        Button(onClick = {
                            Log.d("ModularActivity", "Navigate back to login")
                            onNavigate(Login)
                        }) {
                            Text("Already have account? Login")
                        }
                    }
                }
            }

            entry<ForgotPassword> {
                ContentPurple("Forgot Password Screen") {
                    Column {
                        Button(onClick = {
                            Log.d("ModularActivity", "Password reset requested")
                            onNavigate(Login)
                        }) {
                            Text("Send Reset Email")
                        }
                        Button(onClick = {
                            Log.d("ModularActivity", "Navigate back to login from forgot password")
                            onNavigate(Login)
                        }) {
                            Text("Back to Login")
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun SettingsScreen(onLogout: () -> Unit) {
    ContentRed("Settings Screen") {
        Column {
            Button(onClick = onLogout) {
                Text("Logout")
            }
        }
    }
}
