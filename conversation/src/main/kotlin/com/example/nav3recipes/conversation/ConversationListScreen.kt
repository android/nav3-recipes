package com.example.nav3recipes.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ConversationListScreen(
    onConversationClicked: (ConversationId) -> Unit,
    onConversationFragmentClicked: (ConversationId) -> Unit
) {
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            items(10) { index ->
                val conversationId = index + 1
                val conversationDetail = ConversationId(conversationId)
                val backgroundColor = conversationDetail.color
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { onConversationClicked(conversationDetail) }),
                    headlineContent = {
                        Text(
                            modifier = Modifier.clickable(onClick = { onConversationFragmentClicked(conversationDetail) }),
                            text = "Conversation $conversationId",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = backgroundColor
                    )
                )
            }
        }
    }
}
