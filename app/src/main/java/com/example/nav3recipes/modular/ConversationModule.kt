package com.example.nav3recipes.modular

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import com.example.nav3recipes.content.ContentBase
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.ui.theme.colors
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet



// API
object ConversationList
data class ConversationDetail(val id: Int)

// IMPL
@Module
@InstallIn(SingletonComponent::class)
object ConversationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderBuilder(backStack: SnapshotStateList<Any>): EntryProviderBuilder<Any>.() -> Unit =
        {
            entry<ConversationList> { key ->
                ConversationListScreen(
                    key = key,
                    onConversationClicked = { conversationId ->
                        backStack.add(
                            ConversationDetail(
                                conversationId
                            )
                        )
                    },
                    onProfileClicked = { backStack.add(Profile) }
                )
            }
            entry<ConversationDetail> { key ->
                ConversationDetailScreen(key)
            }
        }
}

@Composable
fun ConversationListScreen(
    key: Any,
    onConversationClicked: (Int) -> Unit,
    onProfileClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(10) { index ->
            val conversation = ConversationDetail(index + 1)

            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onConversationClicked(conversation.id) }),
                headlineContent = {
                    Text(
                        text = "Conversation ${conversation.id}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = conversation.color()
                )
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onProfileClicked) {
                Text("Go to Profile")
            }
        }
    }
}

@Composable
fun ConversationDetailScreen(key: ConversationDetail) {
    ContentBase(
        title = "Conversation Detail Screen: ${key.id}",
        modifier = Modifier.background(key.color())
    )
}
private fun ConversationDetail.color() = colors[this.id % colors.size]