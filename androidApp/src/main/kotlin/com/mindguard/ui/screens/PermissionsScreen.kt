package com.mindguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionsScreen(
    isGranted: Boolean,
    onOpenSettings: () -> Unit,
    onContinue: () -> Unit
) {
    LaunchedEffect(isGranted) { if (isGranted) onContinue() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔐", fontSize = 56.sp, modifier = Modifier.padding(bottom = 16.dp))

        Text(
            "One Permission Needed",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            "MindGuard uses Android's Accessibility API to detect when Instagram Reels opens — entirely on-device. No data ever leaves your phone.",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            "Steps: Find \"MindGuard\" in the list, tap it, enable the toggle.",
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onOpenSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BBB))
        ) {
            Text("Open Accessibility Settings", fontSize = 16.sp)
        }
    }
}
