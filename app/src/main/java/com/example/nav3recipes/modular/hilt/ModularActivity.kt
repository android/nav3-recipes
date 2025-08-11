package com.example.nav3recipes.modular.hilt

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.conversation.ConversationDetailFragmentScreen
import com.example.nav3recipes.conversation.ConversationDetailScreen
import com.example.nav3recipes.conversation.ConversationId
import com.example.nav3recipes.conversation.ConversationListScreen
import com.example.nav3recipes.navigator.ProvideNavBackStack
import com.example.nav3recipes.navigator.Route
import com.example.nav3recipes.profile.ProfileScreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.listOf

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
            val viewModel = hiltViewModel<AppViewModel>()
            val sessionState = viewModel.sessionState.collectAsState().value

            when (sessionState) {
                is SessionState.Initialized -> {
                    val topLevelBackStack = rememberTopLevelBackStack(sessionState.startKey)
                    val navBackStack = remember { NavBackStackImpl(topLevelBackStack) }

                    ProvideNavBackStack(navBackStack) {
                        val backStack = topLevelBackStack.backStack
                        if (backStack.isEmpty()) return@ProvideNavBackStack
                        sessionState.mutate(topLevelBackStack)

                        NavDisplay(
                            backStack = backStack,
                            onBack = {
                                Log.d(TAG, "Back pressed")
                                topLevelBackStack.removeLast()
                            },
                            entryProvider = navGraph(
                                isTabSelected = { tab ->
                                    topLevelBackStack.topLevelKey == tab
                                },
                                onTabSelected = { tab ->
                                    topLevelBackStack.addTopLevel(tab)
                                },
                            )
                        )
                    }
                }

                SessionState.Initializing -> Unit
            }
        }
    }

    @Composable
    private fun <T : Any> navGraph(
        isTabSelected: (Route.Tab) -> Boolean,
        onTabSelected: (Route.Tab) -> Unit,
    ): (T) -> NavEntry<T> {
        // Create movable bottom navigation wrapper that persists across tab changes
        val withNavigationBar = remember {
            movableContentOf { content: @Composable () -> Unit ->
                NavigationBarScaffold(
                    isTabSelected = isTabSelected,
                    onTabSelected = onTabSelected,
                    content = content,
                    tabs = listOf(
                        Route.Tab.Conversations,
                        Route.Tab.MyProfile,
                        Route.Tab.Settings
                    )
                )
            }
        }

        return entryProvider {
            entry<Route.Login> {
                LoginScreen()
            }

            entry<Route.Register> {
                RegisterScreen()
            }

            entry<Route.ForgotPassword> {
                ForgotPasswordScreen()
            }

            // Authenticated screens with movable bottom navigation
            entry<Route.Tab.Conversations> {
                withNavigationBar {
                    ConversationListScreen()
                }
            }

            entry<Route.ConversationDetail> { key ->
                withNavigationBar {
                    ConversationDetailScreen(
                        conversationId = ConversationId(key.id),
                    )
                }
            }

            entry<Route.ConversationDetailFragment> { key ->
                // We do omit withNavigationBar {} on purpose to show case that we can show UI without using tabs
                ConversationDetailFragmentScreen(
                    conversationId = ConversationId(key.id),
                )
            }

            entry<Route.Tab.MyProfile> {
                withNavigationBar {
                    ContentPurple("My Profile") {
                        Text("My Profile")
                    }
                }
            }

            entry<Route.UserProfile> {
                withNavigationBar {
                    ProfileScreen()
                }
            }

            entry<Route.Tab.Settings> {
                withNavigationBar {
                    SettingsScreen()
                }
            }
        }
    }
}


@Composable
private fun NavigationBarScaffold(
    tabs: List<Route.Tab>,
    isTabSelected: (Route.Tab) -> Boolean,
    onTabSelected: (Route.Tab) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
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
