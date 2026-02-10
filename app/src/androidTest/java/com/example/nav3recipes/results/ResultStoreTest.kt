package com.example.nav3recipes.results

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.results.state.ResultStore
import org.junit.Rule
import org.junit.Test

class ResultStoreTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testResultStoreWithDialog() {
        lateinit var backStack: NavBackStack<NavKey>
        composeTestRule.setContent {
            val resultStore = remember { ResultStore() }
            backStack = rememberNavBackStack(Home)
            val dialogStrategy = remember { DialogSceneStrategy<NavKey>() }

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategy = dialogStrategy,
                entryProvider = entryProvider {
                    entry<Home> {
                        val result = resultStore.getResultState<String?>(key)
                        Text(result ?: noResult)
                    }
                    entry<Dialog>(metadata = DialogSceneStrategy.dialog()) {
                        Button(onClick = {
                            resultStore.setResult<String>(
                                resultKey = key,
                                result = resultFromDialog
                            )
                        }) {
                            Text(sendResult)
                        }
                    }
                }
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(noResult).assertIsDisplayed()

        composeTestRule.runOnIdle {
            backStack.add(Dialog)
        }

        composeTestRule.waitForIdle()

        // Send Result
        composeTestRule.onNodeWithText(sendResult).performClick()

        composeTestRule.runOnIdle {
            backStack.removeLastOrNull()
        }

        composeTestRule.waitForIdle()

        // Verify Result
        composeTestRule.onNodeWithText(resultFromDialog).assertIsDisplayed()
    }
}

private const val key = "key"