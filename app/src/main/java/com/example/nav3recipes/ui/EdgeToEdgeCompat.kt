package com.example.nav3recipes.ui

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

fun ComponentActivity.enableEdgeToEdgeCompat() {
    enableEdgeToEdge()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Fix for three-button nav not properly going edge-to-edge.
        // TODO: https://issuetracker.google.com/issues/298296168
        window.isNavigationBarContrastEnforced = false
    }
}
