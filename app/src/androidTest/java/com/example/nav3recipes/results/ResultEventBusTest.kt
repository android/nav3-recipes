package com.example.nav3recipes.results

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.nav3recipes.results.event.ResultEffect
import com.example.nav3recipes.results.event.ResultEventBus
import kotlinx.serialization.Serializable
import org.junit.Rule
import org.junit.Test

class ResultEventBusTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testResultEventBusWithDialog() {
        lateinit var backStack: NavBackStack<NavKey>
        composeTestRule.setContent {
            val resultEventBus = remember { ResultEventBus() }
            backStack = rememberNavBackStack(Home)
            val dialogStrategy = remember { DialogSceneStrategy<NavKey>() }

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategy = dialogStrategy,
                entryProvider = entryProvider {
                    entry<Home> {
                        var result by remember { mutableStateOf(noResult) }
                        ResultEffect<String>(resultEventBus) {
                            result = it
                        }
                        Text(result)
                    }
                    entry<Dialog>(metadata = DialogSceneStrategy.dialog()) {
                        Button(onClick = {
                            resultEventBus.sendResult<String>(result = resultFromDialog)
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

@Serializable
internal data object Home : NavKey

@Serializable
internal data object Dialog : NavKey

internal const val noResult = "No Result"
internal const val resultFromDialog = "Result from Dialog"
internal const val sendResult = "Send Result"