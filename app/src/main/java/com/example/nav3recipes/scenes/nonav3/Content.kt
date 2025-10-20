package com.example.nav3recipes.scenes.nonav3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nav3recipes.ui.theme.colors

@Composable
fun ConversationListScreen(
    onConversationClicked: (ConversationDetail) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(10) { index ->
            val conversationId = index + 1
            val conversationDetail = ConversationDetail(
                id = conversationId,
                colorId = conversationId % colors.size
            )
            val backgroundColor = colors[conversationDetail.colorId]
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onConversationClicked(conversationDetail) }),
                headlineContent = {
                    Text(
                        text = "Conversation $conversationId",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = backgroundColor // Set container color directly
                )
            )
        }
    }
}

@Composable
fun ConversationDetailScreen(
    conversationDetail: ConversationDetail,
    onProfileClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors[conversationDetail.colorId])
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Conversation Detail Screen: ${conversationDetail.id}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onProfileClicked) {
            Text("View Profile")
        }
    }
}

@Composable
fun ProfileScreen() {
    val profileColor = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(profileColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Profile Screen",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}