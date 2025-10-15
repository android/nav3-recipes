package com.example.nav3recipes.deeplink.parseintent.singleModule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Landing page for this recipe. It displays several options users can choose from to formulate
 * the final deeplink request.
 *
 * See [ParseIntentActivity] for how the requested deeplink is handled.
 */
class ParseIntentLandingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            /**
             * UI for deeplink sandbox
             */
            EntryScreen("Sandbox - Build Your Deeplink") {
                TextContent("Base url:\n$PATH_BASE/")
                var showFilterOptions by remember { mutableStateOf(false) }
                val selectedPath = remember { mutableStateOf(MENU_OPTIONS_PATH[KEY_PATH]?.first()) }

                var showQueryOptions by remember { mutableStateOf(false) }
                var selectedFilter by remember { mutableStateOf("") }
                val selectedSearchQuery = remember { mutableStateMapOf<String, String>() }

                // manage path options
                MenuDropDown(
                    menuOptions = MENU_OPTIONS_PATH,
                ) { _, selection ->
                    selectedPath.value = selection
                    when (selection) {
                        PATH_SEARCH -> {
                            showQueryOptions = true
                            showFilterOptions = false
                        }
                        PATH_INCLUDE -> {
                            showQueryOptions = false
                            showFilterOptions = true
                        }
                        else -> {
                            showQueryOptions = false
                            showFilterOptions = false
                        }
                    }
                }

                // manage path filter options, reset state if menu is closed
                LaunchedEffect(showFilterOptions) {
                    selectedFilter = if (showFilterOptions) {
                        MENU_OPTIONS_FILTER.values.first().first()
                    } else {
                        ""
                    }
                }
                if (showFilterOptions) {
                    MenuDropDown(
                        menuOptions = MENU_OPTIONS_FILTER,
                    )  { _, selected ->
                        selectedFilter = selected
                    }
                }

                // manage query options, reset state if menu is closed
                LaunchedEffect(showQueryOptions) {
                    if (showQueryOptions) {
                        val initEntry = MENU_OPTIONS_SEARCH.entries.first()
                        selectedSearchQuery[initEntry.key] = initEntry.value.first()
                    } else {
                        selectedSearchQuery.clear()
                    }
                }
                if (showQueryOptions) {
                    MenuTextInput(
                        menuLabels = MENU_LABELS_SEARCH,
                    ) { label, selected ->
                        selectedSearchQuery[label] = selected
                    }
                    MenuDropDown(
                        menuOptions = MENU_OPTIONS_SEARCH,
                    ) { label, selected ->
                        selectedSearchQuery[label] = selected
                    }
                }

                // form final deeplink url
                val arguments = when (selectedPath.value) {
                    PATH_INCLUDE -> "/${selectedFilter}"
                    PATH_SEARCH -> {
                        buildString {
                            selectedSearchQuery.forEach { entry ->
                                if (entry.value.isNotEmpty()) {
                                    val prefix = if (isEmpty()) "?" else "&"
                                    append("$prefix${entry.key}=${entry.value}")
                                }
                            }
                        }
                    }
                    else -> ""
                }
                val finalUrl = "$PATH_BASE/${selectedPath.value}$arguments"
                TextContent("Final url:\n$finalUrl")
                // deeplink to target
                DeepLinkButton(
                    context = this@ParseIntentLandingActivity,
                    targetActivity = ParseIntentActivity::class.java,
                    deepLinkUrl = finalUrl
                )
            }
        }
    }
}

private const val KEY_PATH = "path"
private val MENU_OPTIONS_PATH = mapOf(
    KEY_PATH to listOf(
        STRING_LITERAL_HOME,
        PATH_INCLUDE,
        PATH_SEARCH,
    ),
)

private val MENU_OPTIONS_FILTER = mapOf(
    UsersKey.FILTER_KEY to listOf(UsersKey.FILTER_OPTION_RECENTLY_ADDED, UsersKey.FILTER_OPTION_ALL),
)

private val MENU_OPTIONS_SEARCH = mapOf(
    SearchKey::firstName.name to listOf(EMPTY, FIRST_NAME_JOHN, FIRST_NAME_TOM, FIRST_NAME_MARY, FIRST_NAME_JULIE),
    SearchKey::location.name to listOf(EMPTY, LOCATION_CA, LOCATION_BC, LOCATION_BR, LOCATION_US)
)

private val MENU_LABELS_SEARCH = listOf(SearchKey::ageMin.name, SearchKey::ageMax.name)


