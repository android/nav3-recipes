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
import com.example.nav3recipes.results.saveable.conflateAsSaveableState
import kotlinx.serialization.Serializable
import org.junit.Rule
import org.junit.Test

class ResultSaveableTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testResultConflateAsSaveableState() {
        lateinit var backStack: NavBackStack<NavKey>
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent {
            backStack = rememberNavBackStack(SaveableHome)
            val dialogStrategy = remember { SaveableDialogStrategy }

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategies = listOf(dialogStrategy),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberResultEventBusNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<SaveableHome> {
                        val resultState by LocalResultEventBus.current.conflateAsSaveableState<String?>(null)
                        Text(resultState ?: noResult)
                    }
                    entry<SaveableDialog>(metadata = DialogSceneStrategy.dialog()) {
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
            backStack.add(SaveableDialog)
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
private data object SaveableHome : NavKey

@Serializable
private data object SaveableDialog : NavKey

private val SaveableDialogStrategy = DialogSceneStrategy<NavKey>()

private const val noResult = "No Result"
private const val resultFromDialog = "Result from Dialog"
private const val sendResult = "Send Result"
