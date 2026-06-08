package com.mindguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    instagramEnabled: Boolean,
    youtubeEnabled: Boolean,
    tiktokEnabled: Boolean,
    snapchatEnabled: Boolean,
    focusScheduleEnabled: Boolean,
    focusStartHour: Int,
    focusEndHour: Int,
    currentStreak: Long,
    bestStreak: Long,
    onInstagramToggle: (Boolean) -> Unit,
    onYoutubeToggle: (Boolean) -> Unit,
    onTiktokToggle: (Boolean) -> Unit,
    onSnapchatToggle: (Boolean) -> Unit,
    onFocusScheduleChange: (enabled: Boolean, startHour: Int, endHour: Int) -> Unit,
    onBack: () -> Unit
) {
    var localFocusEnabled by remember(focusScheduleEnabled) { mutableStateOf(focusScheduleEnabled) }
    var localStart by remember(focusStartHour) { mutableIntStateOf(focusStartHour) }
    var localEnd   by remember(focusEndHour)   { mutableIntStateOf(focusEndHour) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F3FF))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Back", color = Color(0xFF635BBB)) }
            Spacer(modifier = Modifier.weight(1f))
            Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(72.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Blocked Apps ──────────────────────────────────────────────────────
        SectionHeader("🛡️  Blocked Apps")
        SettingsCard {
            AppToggleRow("📸  Instagram Reels",   instagramEnabled, onInstagramToggle)
            HorizontalDivider(color = Color(0xFFF0EEF8))
            AppToggleRow("▶️   YouTube Shorts",   youtubeEnabled,   onYoutubeToggle)
            HorizontalDivider(color = Color(0xFFF0EEF8))
            AppToggleRow("🎵  TikTok",             tiktokEnabled,    onTiktokToggle)
            HorizontalDivider(color = Color(0xFFF0EEF8))
            AppToggleRow("👻  Snapchat Spotlight", snapchatEnabled,  onSnapchatToggle)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Focus Schedule ────────────────────────────────────────────────────
        SectionHeader("⏰  Focus Schedule")
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-block during work hours", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A2E))
                    Text("Override the manual toggle", fontSize = 12.sp, color = Color(0xFF888888))
                }
                Switch(
                    checked = localFocusEnabled,
                    onCheckedChange = { enabled ->
                        localFocusEnabled = enabled
                        onFocusScheduleChange(enabled, localStart, localEnd)
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF635BBB))
                )
            }

            if (localFocusEnabled) {
                HorizontalDivider(color = Color(0xFFF0EEF8))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HourPicker("From", localStart) { h ->
                        localStart = h
                        onFocusScheduleChange(localFocusEnabled, h, localEnd)
                    }
                    Text("to", color = Color(0xFF888888))
                    HourPicker("To", localEnd) { h ->
                        localEnd = h
                        onFocusScheduleChange(localFocusEnabled, localStart, h)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Streak ────────────────────────────────────────────────────────────
        SectionHeader("🔥  Your Streak")
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StreakStat("🔥", "Current", "$currentStreak days")
                VerticalDivider(modifier = Modifier.height(48.dp), color = Color(0xFFF0EEF8))
                StreakStat("🏆", "Best", "$bestStreak days")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF635BBB),
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        content = { Column(content = content) }
    )
}

@Composable
private fun AppToggleRow(label: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, modifier = Modifier.weight(1f), color = Color(0xFF1A1A2E))
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF635BBB))
        )
    }
}

@Composable
private fun HourPicker(label: String, hour: Int, onChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color(0xFF888888))
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange((hour - 1 + 24) % 24) }, modifier = Modifier.size(32.dp)) {
                Text("−", fontSize = 20.sp, color = Color(0xFF635BBB))
            }
            Text(
                "%02d:00".format(hour),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                modifier = Modifier.width(64.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = { onChange((hour + 1) % 24) }, modifier = Modifier.size(32.dp)) {
                Text("+", fontSize = 20.sp, color = Color(0xFF635BBB))
            }
        }
    }
}

@Composable
private fun StreakStat(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        Text(label, fontSize = 12.sp, color = Color(0xFF888888))
    }
}
