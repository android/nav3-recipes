package com.example.nav3recipes.modular

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import com.example.nav3recipes.content.ContentBlue
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

// API
object Profile

// IMPLEMENTATION
@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @IntoSet
    @Provides
    fun provideEntryProviderBuilder( backStack: SnapshotStateList<Any>) : EntryProviderBuilder<Any>.() -> Unit = {
        entry<Profile>{
            ProfileScreen()
        }
    }
}

@Composable
fun ProfileScreen() {
    ContentBlue("Profile Screen") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This is the Profile Screen")
        }
    }
}
