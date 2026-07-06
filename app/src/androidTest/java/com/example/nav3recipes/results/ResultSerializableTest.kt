/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nav3recipes.results

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.rememberResultEventBusNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.test.espresso.IdlingPolicies
import com.example.nav3recipes.results.serializable.conflateAsSerializableState
import kotlinx.serialization.Serializable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class ResultSerializableTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS)
        IdlingPolicies.setIdlingResourceTimeout(60, TimeUnit.SECONDS)
    }

    @Test
    fun testResultConflateAsSerializableState() {
        lateinit var backStack: NavBackStack<NavKey>
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent {
            backStack = rememberNavBackStack(SerializableHome)
            val dialogStrategy = remember { DialogSceneStrategy<NavKey>() }

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategies = listOf(dialogStrategy),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberResultEventBusNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<SerializableHome> {
                        val resultState by LocalResultEventBus.current.conflateAsSerializableState<String?>(null)
                        Text(resultState ?: noResult)
                    }
                    entry<SerializableDialog>(metadata = DialogSceneStrategy.dialog()) {
                        val resultBus = LocalResultEventBus.current
                        Button(onClick = {
                            resultBus.sendResult(result = resultFromDialog)
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
            backStack.add(SerializableDialog)
        }

        composeTestRule.waitForIdle()

        // Send Result
        composeTestRule.onNodeWithText(sendResult).performClick()

        composeTestRule.runOnIdle {
            backStack.removeLastOrNull()
        }

        composeTestRule.waitForIdle()

        // Emulate state/configuration recreation
        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.waitForIdle()

        // Verify Result is successfully retained and displayed!
        composeTestRule.onNodeWithText(resultFromDialog).assertIsDisplayed()
    }
}

@Serializable
private data object SerializableHome : NavKey

@Serializable
private data object SerializableDialog : NavKey

private const val noResult = "No Result"
private const val resultFromDialog = "Result from Dialog"
private const val sendResult = "Send Result"
