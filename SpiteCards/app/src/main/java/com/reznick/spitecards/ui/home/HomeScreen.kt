package com.reznick.spitecards.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onHostGame: () -> Unit,
    onJoinGame: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SpiteCards",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "No ads. No accounts. No internet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Button(
                onClick = onHostGame,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Host Game", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onJoinGame,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Join Game", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = onHostGame, // Solo mode passes a flag; reuses wizard
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solo / Practice")
            }

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onHistory) { Text("History") }
                TextButton(onClick = onSettings) { Text("Settings") }
                TextButton(onClick = onAbout) { Text("About") }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Part of the Spite line · Tap to see other apps",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { onAbout() }
            )
        }
    }
}
