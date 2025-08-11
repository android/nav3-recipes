package com.example.nav3recipes.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = ConversationDetailViewModel.Factory::class)
internal class ConversationDetailViewModel @AssistedInject constructor(
    @Assisted val conversationId: ConversationId,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(conversationId: ConversationId): ConversationDetailViewModel
    }
}
