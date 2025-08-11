package com.example.nav3recipes.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.compose.AndroidFragment
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback

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
    var onProfileClicked = mutableStateOf({})

    private var textView: TextView? = null
    private var openProfile: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val textView = TextView(inflater.context).also { textView = it }
        val openProfile = Button(inflater.context).also {
            it.text = "Profile"
            openProfile = it
        }

        return LinearLayout(inflater.context).also {
            it.orientation = LinearLayout.VERTICAL
            it.addView(textView)
            it.addView(openProfile)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val conversationIdValue = requireArguments().getInt("conversationId")
        val conversationId = ConversationId(conversationIdValue)
        val viewModel by viewModels<ConversationDetailViewModel>(
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<ConversationDetailViewModel.Factory> { factory ->
                    factory.create(conversationId)
                }
            }
        )
        textView?.text = "Conversation ID is: ${viewModel.conversationId.value}"
        openProfile?.setOnClickListener {
            onProfileClicked.value()
        }
    }
}