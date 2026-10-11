package com.example.nav3recipes.modular.hilt

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.scopes.ActivityRetainedScoped

typealias EntryProviderInstaller = EntryProviderScope<NavKey>.() -> Unit

@ActivityRetainedScoped
class Navigator {
    lateinit var backStack : NavBackStack<NavKey>

    fun goTo(destination: NavKey){
        backStack.add(destination)
    }

    fun goBack(){
        backStack.removeLastOrNull()
    }
}