package com.example.nav3recipes

import android.content.Context
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nav3recipes.animations.AnimatedActivity
import com.example.nav3recipes.basic.BasicActivity
import com.example.nav3recipes.basicdsl.BasicDslActivity
import com.example.nav3recipes.basicsaveable.BasicSaveableActivity
import com.example.nav3recipes.commonui.CommonUiActivity
import com.example.nav3recipes.conditional.ConditionalActivity
import com.example.nav3recipes.scenes.twopane.TwoPaneActivity
import com.example.nav3recipes.ui.theme.Nav3RecipesTheme

@OptIn(ExperimentalMaterial3Api::class)
class CatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Nav3RecipesTheme {
                Scaffold (modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.title_activity_launcher)) },
                            colors =  TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }) {
                    padding ->
                    RecipeListView(padding = padding)
                }
            }
        }
    }
}

private data class RecipeInfo(val label: String, val className: String)

private fun buildRecipeInfoList(context : Context) : Array<RecipeInfo> {
    val list = mutableListOf<RecipeInfo>()
    list.add(RecipeInfo(context.getString(R.string.basic_activity_label), BasicActivity::class.java.name))
    list.add(RecipeInfo(context.getString(R.string.basic_dsl_activity_label), BasicDslActivity::class.java.name))
    list.add(RecipeInfo(context.getString(R.string.basic_saveable_activity_label), BasicSaveableActivity::class.java.name))
    list.add(RecipeInfo(context.getString(R.string.common_ui_activity_label), CommonUiActivity::class.java.name))
    list.add(RecipeInfo(context.getString(R.string.conditional_activity_label),ConditionalActivity::class.java.name))
    list.add(RecipeInfo(context.getString(R.string.two_pane_activity_label), TwoPaneActivity::class.java.name))
    list.add(RecipeInfo(context.getString(R.string.animated_activity_label), AnimatedActivity::class.java.name))
    return list.toTypedArray()
}

@Composable
fun RecipeListView(padding: PaddingValues) {
    val context = LocalContext.current
    val recipes = remember{ buildRecipeInfoList(context) }

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)){
        items(recipes) { recipe ->
            ListItem(
                headlineContent = { Text(recipe.label) },
                modifier = Modifier.clickable {
                    val intent = Intent().setClassName(context, recipe.className)
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Nav3RecipesTheme {
        RecipeListView(PaddingValues())
    }
}