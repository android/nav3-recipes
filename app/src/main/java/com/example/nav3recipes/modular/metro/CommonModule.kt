package com.example.nav3recipes.modular.metro

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.SingleIn

typealias EntryProviderInstaller = EntryProviderScope<Any>.() -> Unit

@SingleIn(ActivityScope::class)
class Navigator(startDestination: Any) {
    val backStack : SnapshotStateList<Any> = mutableStateListOf(startDestination)

    fun goTo(destination: Any){
        backStack.add(destination)
    }

    fun goBack(){
        backStack.removeLastOrNull()
    }
}