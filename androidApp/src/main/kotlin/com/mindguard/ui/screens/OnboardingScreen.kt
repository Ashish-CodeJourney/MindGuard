package com.mindguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "MindGuard",
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            "Reclaim your attention from addictive social media.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            "MindGuard detects and blocks endless scrolling on Instagram Reels, YouTube Shorts, and more.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            "Privacy-first. No accounts. No tracking.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp),
            fontSize = 12.sp
        )

        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Continue")
        }
    }
}
