package com.example.nav3recipes.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.content
import dagger.hilt.android.AndroidEntryPoint

@Composable
fun ConversationDetailFragmentScreen(
    conversationId: ConversationId,
    onProfileClicked: () -> Unit
) {
    Scaffold { paddingValues ->
        AndroidFragment<ConversationDetailFragment>(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            arguments = bundleOf(
                "conversationId" to conversationId.value
            ),
            onUpdate = { fragment ->
                fragment.onProfileClicked.value = onProfileClicked
            }
        )
    }
}

@AndroidEntryPoint
internal class ConversationDetailFragment : Fragment() {
    var onProfileClicked = mutableStateOf<() -> Unit>({})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = (arguments ?: Bundle.EMPTY)
        val conversationId = args.getInt("conversationId")
        return content {
            ConversationDetailScreen(
                conversationId = ConversationId(conversationId),
                onProfileClicked = onProfileClicked.value
            )
        }
    }
}