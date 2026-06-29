package com.example.nav3recipes

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import com.example.nav3recipes.migration.atomic.begin.BeginAtomicMigrationActivity
import com.example.nav3recipes.migration.atomic.end.EndAtomicMigrationActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

/**
 * Instrumented navigation tests for the start and end states of the atomic migration guide.
 */
@RunWith(Parameterized::class)
class AtomicMigrationTest(activityClass: Class<out ComponentActivity>) {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule(activityClass)

    companion object {
        @JvmStatic
        @Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(BeginAtomicMigrationActivity::class.java),
                arrayOf(EndAtomicMigrationActivity::class.java)
            )
        }
    }

    @Test
    fun firstScreen_isA() {
        composeTestRule.apply {
            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
            onNodeWithText("Route A title").assertExists()
        }
    }

    @Test
    fun navigateToB_selectsB() {
        composeTestRule.apply {
            onNode(hasText("Route B") and isSelectable()).performClick()
            onNode(hasText("Route B") and isSelectable()).assertIsSelected()
            onNodeWithText("Route B title").assertExists()
        }
    }

    @Test
    fun navigateToA1_keepsASelected() {
        composeTestRule.apply {
            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
            onNodeWithText("Route A title").assertExists()
            onNodeWithText("Go to A1").performClick()
            onNodeWithText("Route A1 title").assertExists()
            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
        }
    }

    @Test
    fun navigateAtoBtoC_selectsCAndShowsContent() {
        composeTestRule.apply {
            onNode(hasText("Route B") and isSelectable()).performClick()
            onNode(hasText("Route B") and isSelectable()).assertIsSelected()
            onNodeWithText("Route B title").assertExists()

            onNode(hasText("Route C") and isSelectable()).performClick()
            onNode(hasText("Route C") and isSelectable()).assertIsSelected()
            onNodeWithText("Route C title").assertExists()
        }
    }

    @Test
    fun navigateAtoB_pressBack_showsA() {
        composeTestRule.apply {
            onNode(hasText("Route B") and isSelectable()).performClick()
            onNode(hasText("Route B") and isSelectable()).assertIsSelected()
            onNodeWithText("Route B title").assertExists()

            Espresso.pressBack()

            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
            onNodeWithText("Route A title").assertExists()
        }
    }

    @Test
    fun navigateAtoA1_pressBack_showsAContent() {
        composeTestRule.apply {
            onNodeWithText("Go to A1").performClick()
            onNodeWithText("Route A1 title").assertExists()
            onNode(hasText("Route A") and isSelectable()).assertIsSelected()

            Espresso.pressBack()

            onNodeWithText("Route A title").assertExists()
            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
        }
    }

    @Test
    fun navigateAtoBtoC_thenBack_showsA() {
        composeTestRule.apply {
            onNode(hasText("Route B") and isSelectable()).performClick()
            onNode(hasText("Route B") and isSelectable()).assertIsSelected()
            onNodeWithText("Route B title").assertExists()

            onNode(hasText("Route C") and isSelectable()).performClick()
            onNode(hasText("Route C") and isSelectable()).assertIsSelected()
            onNodeWithText("Route C title").assertExists()

            Espresso.pressBack()

            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
            onNodeWithText("Route A title").assertExists()
            onNodeWithText("Route B title").assertDoesNotExist()
        }
    }

    /**
     * TODO: Investigate why these dialog tests sometimes fail.
     */
    @Test
    fun navigateToDialogD_onA_showsDialogContentAndDismisses() {
        composeTestRule.apply {

            onNodeWithText("Open dialog D").performClick()
            onNodeWithText("Route D title (dialog)").assertExists()
            Espresso.pressBack()
            onNodeWithText("Route A title").assertExists()
        }
    }

    @Test
    fun navigateToDialogD_onB_showsDialogContentAndDismisses() {
        composeTestRule.apply {

            onNode(hasText("Route B") and isSelectable()).performClick()

            onNodeWithText("Open dialog D").performClick()
            onNodeWithText("Route D title (dialog)").assertExists()
            Espresso.pressBack()
            onNodeWithText("Route B title").assertExists()
        }
    }


    @Test
    fun navigateToDialogD_onC_showsDialogContentAndDismisses() {
        composeTestRule.apply {

            onNode(hasText("Route C") and isSelectable()).performClick()

            onNodeWithText("Open dialog D").performClick()
            onNodeWithText("Route D title (dialog)").assertExists()
            Espresso.pressBack()
            onNodeWithText("Route C title").assertExists()
        }
    }
}
