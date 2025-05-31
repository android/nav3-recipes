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

@Composable
fun buildActivityInfoList() : Array<ActivityInfo> {
    val context = LocalContext.current
    val list = mutableListOf<ActivityInfo>()
    list.add(ActivityInfo(context.getString(R.string.basic_activity_label), "com.example.nav3recipes.basic.BasicActivity"))
    list.add(ActivityInfo(context.getString(R.string.basic_dsl_activity_label), "com.example.nav3recipes.basicdsl.BasicDslActivity"))
    list.add(ActivityInfo(context.getString(R.string.basic_saveable_activity_label), "com.example.nav3recipes.basicsaveable.BasicSaveableActivity"))
    list.add(ActivityInfo(context.getString(R.string.common_ui_activity_label), "com.example.nav3recipes.commonui.CommonUiActivity"))
    list.add(ActivityInfo(context.getString(R.string.conditional_activity_label), "com.example.nav3recipes.conditional.ConditionalActivity"))
    list.add(ActivityInfo(context.getString(R.string.two_pane_activity_label), "com.example.nav3recipes.scenes.twopane.TwoPaneActivity"))
    list.add(ActivityInfo(context.getString(R.string.animated_activity_label), "com.example.nav3recipes.animations.AnimatedActivity"))
    return list.toTypedArray()
}

@Composable
fun ActivityListView(padding: PaddingValues) {
    val activities = buildActivityInfoList()

    LazyColumn (Modifier.padding(padding)){
        items(activities) { activity ->
            ListItem(
                headlineContent = { Text(activity.label) },
                modifier = Modifier.clickable {
                    val intent = Intent().setClassName(LocalContext.current, activity.className)
                    LocalContext.current.startActivity(intent)
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