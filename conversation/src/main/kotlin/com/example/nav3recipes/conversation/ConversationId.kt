package com.example.nav3recipes.conversation

import androidx.compose.ui.graphics.Color
import com.example.nav3recipes.conversation.colors

data class ConversationId(val value: Int) {
    internal val color: Color get() {
        return colors[value % colors.size]
    }
}
