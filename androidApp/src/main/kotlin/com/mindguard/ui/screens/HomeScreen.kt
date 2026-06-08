package com.mindguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private fun formatDuration(seconds: Long): String = when {
    seconds < 60   -> "${seconds}s"
    seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
    else           -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
}

private fun formatPauseTime(pauseUntilMs: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = pauseUntilMs
    return "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
}

@Composable
fun HomeScreen(
    protectionEnabled: Boolean,
    blockCount: Long,
    attemptCount: Long,
    currentStreak: Long,
    pauseUntilMs: Long = 0L,
    onToggleProtection: (Boolean) -> Unit,
    onResumeFromPause: () -> Unit = {},
    onViewStats: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val timeSavedSeconds = blockCount * 90L
    val deflectedPct     = if (attemptCount > 0) (blockCount * 100 / attemptCount) else 0L
    val isPaused = pauseUntilMs > 0L && System.currentTimeMillis() < pauseUntilMs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F3FF))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MindGuard", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF635BBB))
            }
        }

        // Streak badge (only if streak > 0)
        if (currentStreak > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFFFEDD8)
            ) {
                Text(
                    "🔥 $currentStreak day streak — keep it up!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB45309),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Protection card
        val cardColor = when {
            isPaused          -> Color(0xFF7A4F00)
            protectionEnabled -> Color(0xFF1B5E20)
            else              -> Color(0xFF4A1942)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            when {
                                isPaused          -> "⏸️ Paused"
                                protectionEnabled -> "🛡️ Active"
                                else              -> "🔴 Off"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            when {
                                isPaused          -> "Resumes at ${formatPauseTime(pauseUntilMs)}"
                                protectionEnabled -> "Reels & Shorts are being blocked"
                                else              -> "Protection is off"
                            },
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = protectionEnabled && !isPaused,
                        onCheckedChange = {
                            if (isPaused) onResumeFromPause() else onToggleProtection(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.White.copy(alpha = 0.4f)
                        )
                    )
                }
                if (isPaused) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onResumeFromPause,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Resume Now", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2×2 stats grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("🚫", "Reels Blocked",  blockCount.toString(),            Modifier.weight(1f))
            StatCard("⏱️", "Time Saved",     formatDuration(timeSavedSeconds), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("👀", "Attempts",   attemptCount.toString(), Modifier.weight(1f))
            StatCard("🎯", "Deflected",  "$deflectedPct%",        Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onViewStats) {
            Text("View All-Time Stats →", color = Color(0xFF635BBB), fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Text(label, fontSize = 11.sp, color = Color(0xFF888888), textAlign = TextAlign.Center)
        }
    }
}
