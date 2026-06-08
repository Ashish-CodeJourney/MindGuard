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
fun PermissionsScreen(onGranted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Accessibility Permission Required",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            "MindGuard needs Accessibility permission to detect when you open Instagram Reels and other distracting content.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            "Your data stays on your device. No tracking.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp),
            fontSize = 12.sp
        )

        Button(onClick = onGranted, modifier = Modifier.fillMaxWidth()) {
            Text("Grant Permission")
        }
    }
}
