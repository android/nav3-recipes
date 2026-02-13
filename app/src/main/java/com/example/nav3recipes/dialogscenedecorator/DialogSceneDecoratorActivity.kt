package com.example.nav3recipes.dialogscenedecorator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentYellow
import com.example.nav3recipes.scenes.listdetail.ListDetailScene
import com.example.nav3recipes.scenes.listdetail.rememberListDetailSceneStrategy
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable

@Serializable
private data object Main : NavKey

@Serializable
private data object SettingsList : NavKey

@Serializable
private data object SettingsDetail : NavKey

class DialogSceneDecoratorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEdgeToEdgeConfig()

        setContent {
            val backStack = rememberNavBackStack(Main)
            val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()
            val dialogSceneDecoratorStrategy = rememberDialogSceneDecoratorStrategy<NavKey>()

            SharedTransitionLayout {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategy = listDetailSceneStrategy,
                    sceneDecoratorStrategies = listOf(dialogSceneDecoratorStrategy),
                    sharedTransitionScope = this,
                    entryProvider = entryProvider {
                        entry<Main> {
                            ContentGreen("Welcome to Nav3") {
                                Button(onClick = dropUnlessResumed {
                                    backStack.add(SettingsList)
                                }) {
                                    Text("Click to open settings")
                                }
                            }
                        }
                        entry<SettingsList>(
                            metadata = DialogSceneDecoratorStrategy.sceneDialog(
                                DialogProperties(
                                    dismissOnBackPress = false,
                                )
                            ) + ListDetailScene.listPane()
                        ) {
                            ContentBlue(
                                title = "Settings List",
                            ) {
                                Button(onClick = dropUnlessResumed {
                                    if (backStack.last() !is SettingsDetail) {
                                        backStack.add(SettingsDetail)
                                    }
                                }) {
                                    Text("Open detail")
                                }
                            }
                        }
                        entry<SettingsDetail>(
                            metadata = ListDetailScene.detailPane()
                        ) {
                            ContentYellow("Settings Detail")
                        }
                    }
                )
            }
        }
    }
}