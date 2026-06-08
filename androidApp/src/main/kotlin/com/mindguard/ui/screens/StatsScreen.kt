package com.mindguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun formatDuration(seconds: Long): String = when {
    seconds < 60   -> "${seconds}s"
    seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
    else           -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
}

@Composable
fun StatsScreen(
    blockCountToday: Long,
    attemptCountToday: Long,
    totalBlocks: Long,
    totalAttempts: Long,
    currentStreak: Long,
    bestStreak: Long,
    onBack: () -> Unit
) {
    var showToday by remember { mutableStateOf(true) }
    val blocks   = if (showToday) blockCountToday   else totalBlocks
    val attempts = if (showToday) attemptCountToday else totalAttempts
    val slipped  = (attempts - blocks).coerceAtLeast(0)
    val deflPct  = if (attempts > 0) (blocks * 100 / attempts) else 0L
    val blockRate = if (attempts > 0) blocks.toFloat() / attempts else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F3FF))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← Back", color = Color(0xFF635BBB)) }
            Spacer(modifier = Modifier.weight(1f))
            Text("Stats", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(64.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab switcher
        Row(
            modifier = Modifier
                .background(Color(0xFFE8E6FF), RoundedCornerShape(50))
                .padding(4.dp)
        ) {
            listOf("Today" to true, "All Time" to false).forEach { (label, isToday) ->
                val selected = showToday == isToday
                Box(
                    modifier = Modifier
                        .background(
                            if (selected) Color(0xFF635BBB) else Color.Transparent,
                            RoundedCornerShape(50)
                        )
                        .clickable { showToday = isToday }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        label,
                        color = if (selected) Color.White else Color(0xFF635BBB),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero card — time saved
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF635BBB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⏱️ Time Saved", fontSize = 15.sp, color = Color.White.copy(alpha = 0.8f))
                Text(
                    formatDuration(blocks * 90L),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text("~90s per reel blocked", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2×2 detail cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard("🚫", "Reels Blocked",    blocks.toString(),   Modifier.weight(1f))
            DetailCard("👀", "Attempts",          attempts.toString(), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard("🎯", "Deflected",         "$deflPct%",         Modifier.weight(1f))
            DetailCard("😬", "Slipped Through",   slipped.toString(),  Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Block rate bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Block rate", fontSize = 13.sp, color = Color(0xFF666666))
                Text("$deflPct%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF635BBB))
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { blockRate.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF635BBB),
                trackColor = Color(0xFFE8E6FF)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Streak card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥", fontSize = 28.sp)
                    Text("$currentStreak", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    Text("Current streak", fontSize = 11.sp, color = Color(0xFF888888))
                }
                VerticalDivider(modifier = Modifier.height(56.dp), color = Color(0xFFFFE4B5))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆", fontSize = 28.sp)
                    Text("$bestStreak", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    Text("Best streak", fontSize = 11.sp, color = Color(0xFF888888))
                }
            }
        }
    }
}

@Composable
private fun DetailCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
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
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Text(label, fontSize = 11.sp, color = Color(0xFF888888), textAlign = TextAlign.Center)
        }
    }
}
