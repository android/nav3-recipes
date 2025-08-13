package com.example.nav3recipes.modular.hilt

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.nav3recipes.navigator.Route

val Route.Tab.icon: ImageVector
    get() = when (this) {
        is Route.Tab.Conversations -> Icons.Default.Face
        is Route.Tab.MyProfile -> Icons.Default.Person
        is Route.Tab.Settings -> Icons.Default.Settings
    }