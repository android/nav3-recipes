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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.fragment.app.FragmentActivity
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

/**
 * Enhanced modular activity that combines:
 * - Bottom navigation for authenticated users with separate tab stacks
 * - Authentication flow for anonymous users
 * - Conditional navigation based on authentication state
 */

// Anonymous user routes
private data object Welcome
private data object Login
private data object Register
private data object ForgotPassword

// Authenticated user routes - main tabs
private sealed interface AuthenticatedRoute {
    val icon: ImageVector
}

private data object ConversationTab : AuthenticatedRoute {
    override val icon = Icons.Default.Face
}

private data object MyProfileTab : AuthenticatedRoute {
    override val icon = Icons.Default.Person
}

private data object SettingsTab : AuthenticatedRoute {
    override val icon = Icons.Default.Settings
}

// Conversation sub-routes
private data class ConversationDetail(val id: Int)
private data class ConversationDetailFragment(val id: Int)

private data object UserProfile

private val AUTHENTICATED_TABS: List<AuthenticatedRoute> = listOf(
    ConversationTab, MyProfileTab, SettingsTab
)

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
            val authBackStack = remember { AuthBackStack() }

            if (authBackStack.isAuthenticated) {
                Log.d(TAG, "Showing authenticated UI")
                AuthenticatedUI(authBackStack)
            } else {
                Log.d(TAG, "Showing anonymous UI")
                AnonymousUI(authBackStack)
            }
        }
    }
}

@Composable
private fun AuthenticatedUI(authBackStack: AuthBackStack) {
    val topLevelBackStack = remember { TopLevelBackStack<Any>(ConversationTab) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AUTHENTICATED_TABS.forEach { tab ->
                    val isSelected = tab == topLevelBackStack.topLevelKey
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            Log.d("ModularActivity", "Tab clicked: $tab")
                            topLevelBackStack.addTopLevel(tab)
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
            backStack = topLevelBackStack.backStack,
            onBack = {
                Log.d("ModularActivity", "Back pressed in authenticated UI")
                topLevelBackStack.removeLast()
            },
            entryProvider = entryProvider {
                entry<ConversationTab> {
                    ConversationListScreen(
                        onConversationClicked = { conversationId ->
                            Log.d("ModularActivity", "Conversation clicked: $conversationId")
                            topLevelBackStack.add(ConversationDetail(conversationId.value))
                        },
                        onConversationFragmentClicked = { conversationId ->
                            Log.d(
                                "ModularActivity",
                                "Conversation fragment clicked: $conversationId"
                            )
                            topLevelBackStack.add(ConversationDetailFragment(conversationId.value))
                        },
                    )
                }

                entry<ConversationDetail> { key ->
                    ConversationDetailScreen(
                        conversationId = ConversationId(key.id),
                        onProfileClicked = {
                            Log.d("ModularActivity", "Profile clicked from conversation detail")
                            topLevelBackStack.add(UserProfile)
                        }
                    )
                }

                entry<ConversationDetailFragment> { key ->
                    ConversationDetailFragmentScreen(
                        conversationId = ConversationId(key.id),
                        onProfileClicked = {
                            Log.d("ModularActivity", "Profile clicked from conversation fragment")
                            topLevelBackStack.add(UserProfile)
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
                            authBackStack.logout()
                        }
                    )
                }
            },
            modifier = Modifier.padding(contentPadding)
        )
    }
}

@Composable
private fun AnonymousUI(authBackStack: AuthBackStack) {
    NavDisplay(
        backStack = authBackStack.anonymousBackStack,
        onBack = {
            Log.d("ModularActivity", "Back pressed in anonymous UI")
            authBackStack.removeFromAnonymous()
        },
        entryProvider = entryProvider {
            entry<Welcome> {
                ContentGreen("Welcome to Nav3 Recipes") {
                    Column {
                        Button(onClick = {
                            Log.d("ModularActivity", "Login button clicked")
                            authBackStack.addToAnonymous(Login)
                        }) {
                            Text("Login")
                        }
                        Button(onClick = {
                            Log.d("ModularActivity", "Register button clicked")
                            authBackStack.addToAnonymous(Register)
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
                            authBackStack.authenticate()
                        }) {
                            Text("Sign In")
                        }
                        Button(onClick = {
                            Log.d("ModularActivity", "Navigate to register")
                            authBackStack.addToAnonymous(Register)
                        }) {
                            Text("Don't have account? Register")
                        }
                        Button(onClick = {
                            Log.d("ModularActivity", "Navigate to forgot password")
                            authBackStack.addToAnonymous(ForgotPassword)
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
                            authBackStack.authenticate()
                        }) {
                            Text("Create Account")
                        }
                        Button(onClick = {
                            Log.d("ModularActivity", "Navigate back to login")
                            authBackStack.addToAnonymous(Login)
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
                            authBackStack.addToAnonymous(Login)
                        }) {
                            Text("Send Reset Email")
                        }
                        Button(onClick = {
                            Log.d("ModularActivity", "Navigate back to login from forgot password")
                            authBackStack.addToAnonymous(Login)
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

/**
 * Manages authentication state and navigation stacks
 */
class AuthBackStack {
    companion object {
        private const val TAG = "AuthBackStack"
    }

    var isAuthenticated by mutableStateOf(false)
        private set

    val anonymousBackStack = mutableStateListOf<Any>(Welcome)

    fun addToAnonymous(route: Any) {
        Log.d(TAG, "Adding to anonymous stack: $route")
        anonymousBackStack.add(route)
    }

    fun removeFromAnonymous() {
        val removed = anonymousBackStack.removeLastOrNull()
        Log.d(TAG, "Removed from anonymous stack: $removed")
    }

    fun authenticate() {
        Log.d(TAG, "User authenticated")
        isAuthenticated = true
    }

    fun logout() {
        Log.d(TAG, "User logged out")
        isAuthenticated = false
        anonymousBackStack.clear()
        anonymousBackStack.add(Welcome)
    }
}

/**
 * Manages separate navigation stacks for each authenticated tab
 */
class TopLevelBackStack<T : Any>(startKey: T) {
    companion object {
        private const val TAG = "TopLevelBackStack"
    }

    private var topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf(startKey)

    private fun updateBackStack(): SnapshotStateList<T> {
        Log.d(TAG, "Updating back stack")
        return backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
            Log.d(TAG, "New back stack: $backStack")
        }
    }

    fun addTopLevel(key: T) {
        Log.d(TAG, "Adding top level route: $key")

        if (topLevelStacks[key] == null) {
            topLevelStacks[key] = mutableStateListOf(key)
        } else {
            topLevelStacks.remove(key)?.let {
                topLevelStacks[key] = it
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T) {
        Log.d(TAG, "Adding $key to current stack ($topLevelKey)")
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast() {
        Log.d(TAG, "Removing last from current stack ($topLevelKey)")
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()

        if (removedKey != null && topLevelStacks[removedKey] != null) {
            topLevelStacks.remove(removedKey)
        }

        if (topLevelStacks.isNotEmpty()) {
            topLevelKey = topLevelStacks.keys.last()
        }
        updateBackStack()
    }
}
