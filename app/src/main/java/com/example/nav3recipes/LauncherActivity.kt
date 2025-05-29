package com.example.nav3recipes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.nav3recipes.ui.theme.Nav3RecipesTheme

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Nav3RecipesTheme {
                Scaffold (modifier = Modifier.fillMaxSize()) { padding ->
                    ActivityListView(padding = padding)
                }
            }
        }
    }
}

data class ActivityInfo(val label: String, val className: String)

fun buildActivityInfoList() : Array<ActivityInfo> {

    val list = mutableListOf<ActivityInfo>()
    list.add(ActivityInfo("BasicActivity", "com.example.nav3recipes.basic.BasicActivity"))
    list.add(ActivityInfo("BasicDslActivity", "com.example.nav3recipes.basicdsl.BasicDslActivity"))
    list.add(ActivityInfo("BasicSaveableActivity", "com.example.nav3recipes.basicsaveable.BasicSaveableActivity"))
    list.add(ActivityInfo("CommonUiActivity", "com.example.nav3recipes.commonui.CommonUiActivity"))
    list.add(ActivityInfo("ConditionalActivity", "com.example.nav3recipes.conditional.ConditionalActivity"))
    list.add(ActivityInfo("TwoPaneActivity", "com.example.nav3recipes.scenes.twopane.TwoPaneActivity"))
    list.add(ActivityInfo("AnimatedActivity", "com.example.nav3recipes.animations.AnimatedActivity"))
    return list.toTypedArray()
}

@Composable
fun ActivityListView(padding: PaddingValues) {
    val context = LocalContext.current

    val activities = buildActivityInfoList()

    LazyColumn (Modifier.padding(padding)){
        items(activities) { activity ->
            ListItem(
                headlineContent = { Text(activity.label) },
                modifier = Modifier.clickable {
                    val intent = Intent().setClassName(context, activity.className)
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
        ActivityListView(PaddingValues())
    }
}