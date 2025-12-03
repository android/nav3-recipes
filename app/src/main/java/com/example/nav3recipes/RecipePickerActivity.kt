/*
 * Copyright 2025 The Android Open Source Project
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

package com.example.nav3recipes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nav3recipes.animations.AnimatedActivity
import com.example.nav3recipes.basic.BasicActivity
import com.example.nav3recipes.basicdsl.BasicDslActivity
import com.example.nav3recipes.basicsaveable.BasicSaveableActivity
import com.example.nav3recipes.bottomsheet.BottomSheetActivity
import com.example.nav3recipes.commonui.CommonUiActivity
import com.example.nav3recipes.conditional.ConditionalActivity
import com.example.nav3recipes.deeplink.basic.CreateDeepLinkActivity
import com.example.nav3recipes.dialog.DialogActivity
import com.example.nav3recipes.material.listdetail.MaterialListDetailActivity
import com.example.nav3recipes.material.supportingpane.MaterialSupportingPaneActivity
import com.example.nav3recipes.multiplestacks.MultipleStacksActivity
import com.example.nav3recipes.modular.hilt.HiltModularActivity
import com.example.nav3recipes.modular.koin.KoinModularActivity
import com.example.nav3recipes.passingarguments.viewmodels.basic.BasicViewModelsActivity
import com.example.nav3recipes.passingarguments.viewmodels.hilt.HiltViewModelsActivity
import com.example.nav3recipes.passingarguments.viewmodels.koin.KoinViewModelsActivity
import com.example.nav3recipes.results.event.ResultEventActivity
import com.example.nav3recipes.results.state.ResultStateActivity
import com.example.nav3recipes.scenes.listdetail.ListDetailActivity
import com.example.nav3recipes.scenes.twopane.TwoPaneActivity
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import com.example.nav3recipes.deeplink.advanced.AdvancedCreateDeepLinkActivity
import com.example.nav3recipes.scenes.threepane.ThreePaneActivity

/**
 * Activity to show all available recipes and allow users to launch each one.
 */
private class Recipe(
    val name: String,
    val activityClass: Class<out Activity>
)

private class Heading(val name: String)

private val recipes = listOf(
    Heading("Basic API recipes"),
    Recipe("Basic", BasicActivity::class.java),
    Recipe("Basic DSL", BasicDslActivity::class.java),
    Recipe("Basic Saveable", BasicSaveableActivity::class.java),

    Heading("Layouts using Scenes"),
    Recipe("List-detail", ListDetailActivity::class.java),
    Recipe("Two pane", TwoPaneActivity::class.java),
    Recipe("Three pane", ThreePaneActivity::class.java),
    Recipe("Bottom Sheet", BottomSheetActivity::class.java),
    Recipe("Dialog", DialogActivity::class.java),

    Heading("Material adaptive layouts"),
    Recipe("Material list-detail layout", MaterialListDetailActivity::class.java),
    Recipe("Material supporting-pane layout", MaterialSupportingPaneActivity::class.java),

    Heading("Animations"),
    Recipe("NavDisplay and NavEntry animations", AnimatedActivity::class.java),

    Heading("Common use cases"),
    Recipe("Common UI", CommonUiActivity::class.java),
    Recipe("Multiple Stacks", MultipleStacksActivity::class.java),
    Recipe("Conditional navigation", ConditionalActivity::class.java),

    Heading("Architecture"),
    Recipe("Hilt - Modular Navigation", HiltModularActivity::class.java),
    Recipe("Koin - Modular Navigation", KoinModularActivity::class.java),

    Heading("Passing navigation arguments using ViewModels"),
    Recipe("Basic", BasicViewModelsActivity::class.java),
    Recipe("Using Hilt", HiltViewModelsActivity::class.java),
    Recipe("Using Koin", KoinViewModelsActivity::class.java),

    Heading("Returning Results"),
    Recipe("Return result as Event", ResultEventActivity::class.java),
    Recipe("Return result as State", ResultStateActivity::class.java),

    Heading("Deeplink"),
    Recipe("Parse Intent", CreateDeepLinkActivity::class.java),
    Recipe("Synthetic BackStack", AdvancedCreateDeepLinkActivity::class.java),
)

class RecipePickerActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEdgeToEdgeConfig()
        setContent {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text("Recipes") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }) { innerPadding ->
                RecipeList(padding = innerPadding)
            }
        }
    }


    @Composable
    fun RecipeList(padding: PaddingValues) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recipes) { item ->
                when(item){
                    is Recipe -> {
                        ListItem(
                            headlineContent = { Text(item.name) },
                            modifier = Modifier.clickable {
                                item.start()
                            }
                        )
                    }
                    is Heading -> {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            modifier = Modifier.height(48.dp),
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }

    private fun Recipe.start(){
        val intent = Intent(this@RecipePickerActivity, this.activityClass)
        startActivity(intent)
    }
}
