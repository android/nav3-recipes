package com.example.nav3recipes.modular.metro

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(ActivityScope::class)
@BindingContainer
object AppModule {

    @Provides
    @SingleIn(ActivityScope::class)
    fun provideNavigator() : Navigator = Navigator(startDestination = ConversationList)
}
