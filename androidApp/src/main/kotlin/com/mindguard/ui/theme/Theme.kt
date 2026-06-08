package com.mindguard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MindGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF635BBB),
            onPrimary = Color.White,
            secondary = Color(0xFF8B83D9),
            background = Color(0xFFF8F8FF),
            surface = Color(0xFFFFFFFF)
        ),
        content = content
    )
}
