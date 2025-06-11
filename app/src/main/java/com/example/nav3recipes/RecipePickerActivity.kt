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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nav3recipes.animations.AnimatedActivity
import com.example.nav3recipes.basic.BasicActivity
import com.example.nav3recipes.basicdsl.BasicDslActivity
import com.example.nav3recipes.basicsaveable.BasicSaveableActivity
import com.example.nav3recipes.commonui.CommonUiActivity
import com.example.nav3recipes.conditional.ConditionalActivity
import com.example.nav3recipes.modular.ModularActivity
import com.example.nav3recipes.passingarguments.basicviewmodels.BasicViewModelsActivity
import com.example.nav3recipes.passingarguments.injectedviewmodels.InjectedViewModelsActivity
import com.example.nav3recipes.scenes.materiallistdetail.MaterialListDetailActivity
import com.example.nav3recipes.scenes.twopane.TwoPaneActivity
import com.example.nav3recipes.ui.setEdgeToEdgeConfig

/**
 * Activity to show all available recipes and allow users to launch each one.
 */
private class Recipe(
    val name: String,
    val activityClass: Class<out Activity>
)

private val recipes = listOf(
    Recipe("Basic", BasicActivity::class.java),
    Recipe("Basic DSL", BasicDslActivity::class.java),
    Recipe("Basic Saveable", BasicSaveableActivity::class.java),
    Recipe("Animations", AnimatedActivity::class.java),
    Recipe("Conditional navigation", ConditionalActivity::class.java),
    Recipe("Common UI", CommonUiActivity::class.java),
    Recipe("Material list-detail layout", MaterialListDetailActivity::class.java),
    Recipe("Two pane layout", TwoPaneActivity::class.java),
    Recipe("Argument passing to basic ViewModel", BasicViewModelsActivity::class.java),
    Recipe("Argument passing to injected ViewModel", InjectedViewModelsActivity::class.java),
    Recipe("Modular Navigation", ModularActivity::class.java)
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
                RecipeListView(padding = innerPadding)
            }
        }
    }


    @Composable
    fun RecipeListView(padding: PaddingValues) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recipes) { recipe ->
                ListItem(
                    headlineContent = { Text(recipe.name) },
                    modifier = Modifier.clickable {
                        recipe.start()
                    }
                )
            }
        }
    }

    private fun Recipe.start(){
        val intent = Intent(this@RecipePickerActivity, this.activityClass)
        startActivity(intent)
    }
}
