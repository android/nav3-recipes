package com.example.nav3recipes.modular

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Provides
    @Singleton
    fun provideBackStackFactory() : BackStackFactory = BackStackFactory()
}

class BackStackFactory @Inject constructor() {
    fun create(startDestination: Any) : SnapshotStateList<Any> = mutableStateListOf(startDestination)
}
