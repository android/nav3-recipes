package com.example.nav3recipes.deeplink.basic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.dropUnlessResumed
import com.example.nav3recipes.deeplink.common.PaddedButton
import com.example.nav3recipes.deeplink.common.EMPTY
import com.example.nav3recipes.deeplink.common.EntryScreen
import com.example.nav3recipes.deeplink.common.FIRST_NAME_JOHN
import com.example.nav3recipes.deeplink.common.FIRST_NAME_JULIE
import com.example.nav3recipes.deeplink.common.FIRST_NAME_MARY
import com.example.nav3recipes.deeplink.common.FIRST_NAME_TOM
import com.example.nav3recipes.deeplink.common.LOCATION_BC
import com.example.nav3recipes.deeplink.common.LOCATION_BR
import com.example.nav3recipes.deeplink.common.LOCATION_CA
import com.example.nav3recipes.deeplink.common.LOCATION_US
import com.example.nav3recipes.deeplink.common.MenuDropDown
import com.example.nav3recipes.deeplink.common.MenuTextInput
import com.example.nav3recipes.deeplink.basic.ui.PATH_BASE
import com.example.nav3recipes.deeplink.basic.ui.PATH_INCLUDE
import com.example.nav3recipes.deeplink.basic.ui.PATH_SEARCH
import com.example.nav3recipes.deeplink.basic.ui.STRING_LITERAL_HOME
import com.example.nav3recipes.deeplink.common.TextContent

/**
 * This activity allows the user to create a deep link and make a request with it.
 *
 * **HOW THIS RECIPE WORKS** it consists of two activities - [CreateDeepLinkActivity] to construct
 * and trigger the deeplink request, and the [MainActivity] to show how an app can handle
 * that request.
 *
 * **DEMONSTRATED FORMS OF DEEPLINK** The [MainActivity] has a several backStack keys to
 * demonstrate different types of supported deeplinks:
 * 1. [HomeKey] - deeplink with an exact url (no deeplink arguments)
 * 2. [UsersKey] - deeplink with path arguments
 * 3. [SearchKey] - deeplink with query arguments
 * See [MainActivity.deepLinkPatterns] for the actual url pattern of each.
 *
 * **RECIPE STRUCTURE** This recipe consists of three main packages:
 * 1. basic.deeplink - Contains the two activities
 * 2. basic.deeplink.ui - Contains the activity UI code, i.e. global string variables, deeplink URLs etc
 * 3. basic.deeplink.util - Contains the classes and helper methods to parse and match
 * the deeplinks
 *
 * See [MainActivity] for how the requested deeplink is handled.
 */
class CreateDeepLinkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            /**
             * UI for deeplink sandbox
             */
            EntryScreen("Sandbox - Build Your Deeplink") {
                TextContent("Base url:\n${PATH_BASE}/")
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
                    ) { _, selected ->
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
                val finalUrl = "${PATH_BASE}/${selectedPath.value}$arguments"
                TextContent("Final url:\n$finalUrl")
                // deeplink to target
                PaddedButton("Deeplink Away!", onClick = dropUnlessResumed {
                    val intent = Intent(
                        this@CreateDeepLinkActivity,
                        MainActivity::class.java
                    )
                    // start activity with the url
                    intent.data = finalUrl.toUri()
                    startActivity(intent)
                })
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
    SearchKey::firstName.name to listOf(
        EMPTY,
        FIRST_NAME_JOHN,
        FIRST_NAME_TOM,
        FIRST_NAME_MARY,
        FIRST_NAME_JULIE
    ),
    SearchKey::location.name to listOf(EMPTY, LOCATION_CA, LOCATION_BC, LOCATION_BR, LOCATION_US)
)

private val MENU_LABELS_SEARCH = listOf(SearchKey::ageMin.name, SearchKey::ageMax.name)


