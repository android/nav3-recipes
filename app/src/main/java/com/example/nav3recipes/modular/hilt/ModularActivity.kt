package com.example.nav3recipes.modular.hilt

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
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
            val backStack = rememberNavBackStack(Welcome)
            val viewModel =
                hiltViewModel<NavigationViewModel, NavigationViewModel.Factory> { factory ->
                    factory.create(backStack)
                }
            val navigationState = viewModel.navigationState.collectAsState().value
            // Create movable bottom navigation wrapper that persists across tab changes
            val authenticatedWrapper = remember {
                movableContentOf { content: @Composable () -> Unit ->
                    AuthenticatedScaffold(
                        isTabSelected = { tab ->
                            val currentTab = if (navigationState is NavigationState.Authenticated) {
                                navigationState.currentTab
                            } else {
                                ConversationTab
                            }
                            currentTab == tab
                        },
                        onTabSelected = viewModel::navigateToTab,
                        content = content
                    )
                }
            }

            // Always use NavDisplay with the actual backStack from Navigation 3
            NavDisplay(
                backStack = backStack,
                onBack = {
                    Log.d(TAG, "Back pressed")
                    if (!viewModel.navigateBack()) {
                        // If ViewModel can't handle back, let the system handle it
                        backStack.removeLastOrNull()
                    }
                },
                entryProvider = entryProvider {
                    // Welcome screen - shown during initialization
                    entry<Welcome> {
                        WelcomeScreen(
                            navigationState = navigationState,
                            onNavigate = viewModel::navigateToEntry
                        )
                    }

                    // Anonymous screens
                    entry<Login> {
                        ContentBlue("Login Screen") {
                            Column {
                                Button(onClick = {
                                    Log.d(TAG, "User authenticated")
                                    viewModel.authenticate()
                                }) {
                                    Text("Sign In")
                                }
                                Button(onClick = {
                                    Log.d(TAG, "Navigate to register")
                                    viewModel.navigateToEntry(Register)
                                }) {
                                    Text("Don't have account? Register")
                                }
                                Button(onClick = {
                                    Log.d(TAG, "Navigate to forgot password")
                                    viewModel.navigateToEntry(ForgotPassword)
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
                                    Log.d(TAG, "Registration completed")
                                    viewModel.authenticate()
                                }) {
                                    Text("Create Account")
                                }
                                Button(onClick = {
                                    Log.d(TAG, "Navigate back to login")
                                    viewModel.navigateToEntry(Login)
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
                                    Log.d(TAG, "Password reset requested")
                                    viewModel.navigateToEntry(Login)
                                }) {
                                    Text("Send Reset Email")
                                }
                                Button(onClick = {
                                    Log.d(TAG, "Navigate back to login from forgot password")
                                    viewModel.navigateToEntry(Login)
                                }) {
                                    Text("Back to Login")
                                }
                            }
                        }
                    }

                    // Authenticated screens with movable bottom navigation
                    entry<ConversationTab> {
                        authenticatedWrapper {
                            ConversationListScreen(
                                onConversationClicked = { conversationId ->
                                    Log.d(TAG, "Conversation clicked: $conversationId")
                                    viewModel.navigateToEntry(ConversationDetail(conversationId.value))
                                },
                                onConversationFragmentClicked = { conversationId ->
                                    Log.d(TAG, "Conversation fragment clicked: $conversationId")
                                    viewModel.navigateToEntry(
                                        ConversationDetailFragment(
                                            conversationId.value
                                        )
                                    )
                                },
                            )
                        }
                    }

                    entry<ConversationDetail> { key ->
                        authenticatedWrapper {
                            ConversationDetailScreen(
                                conversationId = ConversationId(key.id),
                                onProfileClicked = {
                                    Log.d(TAG, "Profile clicked from conversation detail")
                                    viewModel.navigateToEntry(UserProfile)
                                }
                            )
                        }
                    }

                    entry<ConversationDetailFragment> { key ->
                        // We do omit authenticatedWrapper on purpose to show case that we can show UI without using tabs
                        ConversationDetailFragmentScreen(
                            conversationId = ConversationId(key.id),
                            onProfileClicked = {
                                Log.d(TAG, "Profile clicked from conversation fragment")
                                viewModel.navigateToEntry(UserProfile)
                            }
                        )
                    }

                    entry<MyProfileTab> {
                        authenticatedWrapper {
                            ContentPurple("My Profile") {
                                Text("My Profile")
                            }
                        }
                    }

                    entry<UserProfile> {
                        authenticatedWrapper {
                            ProfileScreen()
                        }
                    }

                    entry<SettingsTab> {
                        authenticatedWrapper {
                            ContentRed("Settings Screen") {
                                Column {
                                    Button(onClick = {
                                        Log.d(TAG, "User logged out")
                                        viewModel.logout()
                                    }) {
                                        Text("Logout")
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun WelcomeScreen(
    navigationState: NavigationState,
    onNavigate: (NavigationEntry) -> Unit
) {
    when (navigationState) {
        is NavigationState.Initializing -> {
            ContentGreen("Loading...") {
                Text("Checking session...")
            }
        }

        is NavigationState.Anonymous -> {
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

        is NavigationState.Authenticated -> {
            // This shouldn't happen on Welcome screen, but handle gracefully
            ContentGreen("Redirecting...") {
                Text("Loading authenticated content...")
            }
        }
    }
}

@Composable
private fun AuthenticatedScaffold(
    isTabSelected: (AuthenticatedTab) -> Boolean,
    onTabSelected: (AuthenticatedTab) -> Unit,
    content: @Composable () -> Unit
) {
    val authenticatedTabs = listOf(ConversationTab, MyProfileTab, SettingsTab)
    Scaffold(
        bottomBar = {
            NavigationBar {
                authenticatedTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = isTabSelected(tab),
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
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}
