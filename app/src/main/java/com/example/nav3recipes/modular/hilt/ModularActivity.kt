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
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
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
            val viewModel = hiltViewModel<AuthViewModel>()
            val sessionState = viewModel.sessionState.collectAsState().value

            when (sessionState) {
                is SessionState.Initialized -> {
                    val topLevelBackStack = rememberTopLevelBackStack(sessionState.startKey)
                    val backStack = topLevelBackStack.backStack
                    sessionState.mutate(topLevelBackStack)

                    NavDisplay(
                        backStack = backStack,
                        onBack = {
                            Log.d(TAG, "Back pressed")
                            topLevelBackStack.removeLast()
                        },
                        entryProvider = navGraph(topLevelBackStack, viewModel)
                    )
                }

                SessionState.Initializing -> Unit
            }
        }
    }

    @Composable
    private fun <T : Any> navGraph(
        topLevelBackStack: TopLevelBackStack<NavigationEntry>,
        viewModel: AuthViewModel
    ): (T) -> NavEntry<T> {
        // Create movable bottom navigation wrapper that persists across tab changes
        val withNavigationBar = remember {
            movableContentOf { content: @Composable () -> Unit ->
                NavigationBarScaffold(
                    isTabSelected = { tab ->
                        topLevelBackStack.topLevelKey == tab
                    },
                    onTabSelected = { tab ->
                        topLevelBackStack.addTopLevel(tab)
                    },
                    content = content
                )
            }
        }

        return entryProvider {
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
                            topLevelBackStack.add(Register)
                        }) {
                            Text("Don't have account? Register")
                        }
                        Button(onClick = {
                            Log.d(TAG, "Navigate to forgot password")
                            topLevelBackStack.add(ForgotPassword)
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
                            topLevelBackStack.add(Login)
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
                            topLevelBackStack.add(Login)
                        }) {
                            Text("Send Reset Email")
                        }
                        Button(onClick = {
                            Log.d(TAG, "Navigate back to login from forgot password")
                            topLevelBackStack.add(Login)
                        }) {
                            Text("Back to Login")
                        }
                    }
                }
            }

            // Authenticated screens with movable bottom navigation
            entry<ConversationTab> {
                withNavigationBar {
                    ConversationListScreen(
                        onConversationClicked = { conversationId ->
                            Log.d(TAG, "Conversation clicked: $conversationId")
                            topLevelBackStack.add(ConversationDetail(conversationId.value))
                        },
                        onConversationFragmentClicked = { conversationId ->
                            Log.d(TAG, "Conversation fragment clicked: $conversationId")
                            topLevelBackStack.add(
                                ConversationDetailFragment(
                                    conversationId.value
                                )
                            )
                        },
                    )
                }
            }

            entry<ConversationDetail> { key ->
                withNavigationBar {
                    ConversationDetailScreen(
                        conversationId = ConversationId(key.id),
                        onProfileClicked = {
                            Log.d(TAG, "Profile clicked from conversation detail")
                            topLevelBackStack.add(UserProfile)
                        }
                    )
                }
            }

            entry<ConversationDetailFragment> { key ->
                // We do omit withNavigationBar {} on purpose to show case that we can show UI without using tabs
                ConversationDetailFragmentScreen(
                    conversationId = ConversationId(key.id),
                    onProfileClicked = {
                        Log.d(TAG, "Profile clicked from conversation fragment")
                        topLevelBackStack.add(UserProfile)
                    }
                )
            }

            entry<MyProfileTab> {
                withNavigationBar {
                    ContentPurple("My Profile") {
                        Text("My Profile")
                    }
                }
            }

            entry<UserProfile> {
                withNavigationBar {
                    ProfileScreen()
                }
            }

            entry<SettingsTab> {
                withNavigationBar {
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
    }
}


@Composable
private fun NavigationBarScaffold(
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
