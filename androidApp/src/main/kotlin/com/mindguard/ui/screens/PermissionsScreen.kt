package com.mindguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    needsRestrictedSettingsStep: Boolean,
    onOpenAppInfo: () -> Unit,
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
            "Permission Setup",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            "MindGuard uses Android's Accessibility API to detect short-video feeds (Reels, Shorts, Spotlight) — entirely on-device. No data leaves your phone.",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = Color(0xFF444444),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (needsRestrictedSettingsStep) {
            StepCard(
                number = "1",
                title = "Allow Restricted Settings",
                description = "Android 13+ requires this extra step for apps installed outside the Play Store.",
                steps = listOf(
                    "Tap \"Open App Info\" below",
                    "Tap the ⋮ menu in the top-right corner",
                    "Tap \"Allow restricted settings\"",
                    "Come back here when done"
                ),
                buttonLabel = "Open App Info",
                buttonColor = Color(0xFF1565C0),
                onClick = onOpenAppInfo
            )

            Spacer(modifier = Modifier.height(16.dp))

            StepCard(
                number = "2",
                title = "Enable Accessibility Access",
                description = "After step 1 is done, open Accessibility Settings and enable MindGuard.",
                steps = listOf(
                    "Find \"MindGuard\" in the list",
                    "Tap it and enable the toggle"
                ),
                buttonLabel = "Open Accessibility Settings",
                buttonColor = Color(0xFF635BBB),
                onClick = onOpenSettings
            )
        } else {
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
}

@Composable
private fun StepCard(
    number: String,
    title: String,
    description: String,
    steps: List<String>,
    buttonLabel: String,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(buttonColor, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(description, fontSize = 13.sp, color = Color(0xFF555555))
            Spacer(modifier = Modifier.height(8.dp))

            steps.forEachIndexed { i, step ->
                Text(
                    "${i + 1}. $step",
                    fontSize = 13.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text(buttonLabel, fontSize = 14.sp)
            }
        }
    }
}
