package com.example.nav3recipes.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nav3recipes.navigator.LocalNavBackStack
import com.example.nav3recipes.navigator.Route

@Composable
fun ConversationDetailScreen(
    conversationId: ConversationId,
) {
    // Use assisted Hilt ViewModel factory to pass conversationId.value to SavedStateHandle
    val viewModel: ConversationDetailViewModel =
        hiltViewModel<ConversationDetailViewModel, ConversationDetailViewModel.Factory> { factory ->
            factory.create(conversationId)
        }

    ConversationDetailScreenImpl(
        viewModel = viewModel,
    )
}

@Composable
private fun ConversationDetailScreenImpl(
    viewModel: ConversationDetailViewModel,
) {
    val navBackStack = LocalNavBackStack.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(viewModel.conversationId.color)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Conversation Detail Screen: ${viewModel.conversationId.value}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navBackStack.add(Route.UserProfile)
        }) {
            Text("View Profile")
        }
    }
}
