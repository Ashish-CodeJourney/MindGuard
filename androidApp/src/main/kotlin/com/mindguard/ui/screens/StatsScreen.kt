package com.mindguard.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
    instagramBlocks: Long = 0L,
    youtubeBlocks: Long = 0L,
    tiktokBlocks: Long = 0L,
    snapchatBlocks: Long = 0L,
    onBack: () -> Unit
) {
    var showToday by remember { mutableStateOf(true) }
    val blocks   = if (showToday) blockCountToday   else totalBlocks
    val attempts = if (showToday) attemptCountToday else totalAttempts
    val slipped  = (attempts - blocks).coerceAtLeast(0)
    val deflPct  = if (attempts > 0) (blocks * 100 / attempts) else 0L
    val blockRate = if (attempts > 0) blocks.toFloat() / attempts else 0f

    val appData = listOf(
        AppStat("Instagram", instagramBlocks, Color(0xFFE1306C)),
        AppStat("YouTube",   youtubeBlocks,   Color(0xFFFF0000)),
        AppStat("TikTok",    tiktokBlocks,    Color(0xFF444444)),
        AppStat("Snapchat",  snapchatBlocks,  Color(0xFFFFCC00))
    )
    val totalAppBlocks = appData.sumOf { it.count }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F3FF))
            .verticalScroll(rememberScrollState())
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
                Text("~90s per video blocked", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard("🚫", "Videos Blocked",  blocks.toString(),   Modifier.weight(1f))
            DetailCard("👀", "Attempts",         attempts.toString(), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard("🎯", "Deflected",        "$deflPct%",         Modifier.weight(1f))
            DetailCard("😬", "Slipped Through",  slipped.toString(),  Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    "App Breakdown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    "All-time distribution",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (totalAppBlocks == 0L) {
                    Text(
                        "No blocks recorded yet — the chart will appear once you get started!",
                        fontSize = 14.sp,
                        color = Color(0xFF888888),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppPieChart(
                            data = appData,
                            total = totalAppBlocks,
                            modifier = Modifier.size(130.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            appData.filter { it.count > 0 }.forEach { app ->
                                val pct = (app.count * 100 / totalAppBlocks).toInt()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(app.color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            app.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1A1A2E)
                                        )
                                        Text(
                                            "${app.count} blocks · $pct%",
                                            fontSize = 11.sp,
                                            color = Color(0xFF888888)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    appData.forEach { app ->
                        AppChip(app = app, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private data class AppStat(val name: String, val count: Long, val color: Color)

@Composable
private fun AppPieChart(data: List<AppStat>, total: Long, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.width * 0.2f
        val radius = (size.minDimension - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2f, radius * 2f)

        var startAngle = -90f
        val gap = 3f

        data.filter { it.count > 0 }.forEach { app ->
            val sweep = (app.count.toFloat() / total.toFloat()) * 360f - gap
            drawArc(
                color = app.color,
                startAngle = startAngle,
                sweepAngle = sweep.coerceAtLeast(0f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweep + gap
        }
    }
}

@Composable
private fun AppChip(app: AppStat, modifier: Modifier = Modifier) {
    val emoji = when (app.name) {
        "Instagram" -> "📸"
        "YouTube"   -> "▶️"
        "TikTok"    -> "🎵"
        "Snapchat"  -> "👻"
        else        -> "📱"
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = app.color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                app.count.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Text(
                app.name,
                fontSize = 9.sp,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
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
