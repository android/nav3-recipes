package com.example.nav3recipes.deeplink.advanced

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.common.deeplink.EntryScreen
import com.example.nav3recipes.common.deeplink.FriendsList
import com.example.nav3recipes.common.deeplink.LIST_USERS
import com.example.nav3recipes.common.deeplink.PaddedButton
import com.example.nav3recipes.deeplink.advanced.util.buildBackStack
import com.example.nav3recipes.deeplink.advanced.util.navigateUp
import com.example.nav3recipes.deeplink.advanced.util.toKey

class AdvancedDeeplinkAppActivity: ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val startKey = intent.data.toKey()

        val flags = intent.flags
        val isNewTask = flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0 &&
                flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != 0

        val syntheticBackStack = buildBackStack(
            startKey = startKey,
            buildFullPath = isNewTask
        )
        setContent {
            val backStack: NavBackStack<NavKey> = rememberNavBackStack(*(syntheticBackStack.toTypedArray()))

            Scaffold(
                topBar = {
                    // top app bar to display up button
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                        title = { stringResource(R.string.app_name)},
                        navigationIcon = {
                            /**
                             * Up button should never exit your app. Do not display it
                             * on the root Screen.
                             */
                            if (backStack.last() != Home) {
                                IconButton(onClick = {
                                    backStack.navigateUp(
                                        this@AdvancedDeeplinkAppActivity,
                                        this@AdvancedDeeplinkAppActivity
                                    )
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_arrow_upward_24),
                                        contentDescription = "Up Button",
                                    )
                                }
                            }
                        },
                    )
                },
            ) { innerPadding ->
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull()},
                    modifier = Modifier.padding(innerPadding),
                    entryProvider = entryProvider {
                        entry<Home> { key ->
                            EntryScreen(key.screenTitle) {
                                PaddedButton("See Users") {
                                    backStack.add(Users)
                                }
                            }
                        }
                        entry<Users> { key ->
                            EntryScreen(key.screenTitle) {
                                FriendsList(LIST_USERS) { user ->
                                    backStack.add(UserDetail(user))
                                }
                            }
                        }
                        entry<UserDetail> { result ->
                            EntryScreen(result.screenTitle) {
                                FriendsList(listOf(result.user))
                            }
                        }
                    }
                )
            }
        }
    }
}