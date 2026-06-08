package com.mindguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    protectionEnabled: Boolean,
    blockCount: Long,
    onToggleProtection: (Boolean) -> Unit,
    onViewStats: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(protectionEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "MindGuard",
            fontSize = 32.sp,
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Protection")
            Switch(
                checked = isEnabled,
                onCheckedChange = { enabled ->
                    isEnabled = enabled
                    onToggleProtection(enabled)
                }
            )
        }

        Text(
            if (isEnabled) "Protection ON" else "Protection OFF",
            fontSize = 18.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            "$blockCount blocks today",
            fontSize = 24.sp,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Text(
            "Time saved: ~${blockCount * 30}s",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onViewStats, modifier = Modifier.fillMaxWidth()) {
            Text("View Stats")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
