package com.example.nav3recipes.modular

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

// API
object ConversationList
data class ConversationDetail(val id: String)

// IMPL
@Module
@InstallIn(SingletonComponent::class)
object ConversationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderBuilder( backStack: SnapshotStateList<Any>) : EntryProviderBuilder<Any>.() -> Unit = {
        entry<ConversationList>{
            ConversationListScreen(
                onConversationClicked = { conversationId -> backStack.add(ConversationDetail(conversationId))},
                onProfileClicked = { backStack.add(Profile) }
            )
        }
        entry<ConversationDetail>{
            ConversationDetailScreen(it.id)
        }
    }
}

@Composable
fun ConversationListScreen(
    onConversationClicked: (String) -> Unit,
    onProfileClicked: () -> Unit
) {
    ContentGreen("Conversation List Screen") {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { onConversationClicked("Conversation 1") }) {
                Text("Go to Conversation 1 Detail")
            }
            Button(onClick = { onConversationClicked("Conversation 2") }) {
                Text("Go to Conversation 2 Detail")
            }
            Button(onClick = onProfileClicked) {
                Text("Go to Profile")
            }
        }
    }
}

@Composable
fun ConversationDetailScreen(conversationId: String) {
    ContentBlue("Conversation Detail Screen: $conversationId") {
        // Content for conversation detail
    }
}
