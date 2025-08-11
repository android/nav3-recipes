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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
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
