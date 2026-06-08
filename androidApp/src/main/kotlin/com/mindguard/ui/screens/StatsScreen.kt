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
fun StatsScreen(
    totalBlocksToday: Long,
    totalBlocksWeek: Long,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Block Statistics",
            fontSize = 28.sp,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Card(title = "Today", blocks = totalBlocksToday)
        Card(title = "This Week", blocks = totalBlocksWeek)

        Text(
            "Time saved today: ~${totalBlocksToday * 30}s",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Text(
            "Time saved this week: ~${totalBlocksWeek * 30}s",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun Card(title: String, blocks: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 14.sp)
        Text("$blocks blocks", fontSize = 24.sp)
    }
}
