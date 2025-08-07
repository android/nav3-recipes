package com.example.nav3recipes.modular.hilt

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@ActivityRetainedScoped
class INavigatorImpl(startDestination: Any) : INavigator {
    val backStack: SnapshotStateList<Any> = mutableStateListOf(startDestination)

    override fun goTo(destination: Any) {
        backStack.add(destination)
    }

    override fun goBack() {
        backStack.removeLastOrNull()
    }

    @dagger.Module
    @InstallIn(ActivityRetainedComponent::class)
    object Module {
        @Provides
        @ActivityRetainedScoped
        fun provideNavigator(): INavigatorImpl =
            INavigatorImpl(startDestination = Route.ConversationList)

        @Provides
        @ActivityRetainedScoped
        fun provideNavigatorApi(nav: INavigatorImpl): INavigator = nav
    }
}