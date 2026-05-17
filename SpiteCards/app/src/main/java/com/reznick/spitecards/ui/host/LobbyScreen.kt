package com.reznick.spitecards.ui.host

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reznick.spitecards.data.model.Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    onGameStart: (String) -> Unit,
    onBack: () -> Unit
) {
    // In a real implementation these come from NearbyConnectionsManager via a ViewModel
    val players = remember { mutableStateListOf<Player>() }
    val joinCode = remember { (1000..9999).random().toString() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Waiting for players…") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Join code", style = MaterialTheme.typography.titleLarge)
            Text(
                text = joinCode,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            if (players.isEmpty()) {
                Text(
                    "No players yet. Share the code or QR to invite others.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(players) { player ->
                        ListItem(
                            headlineContent = { Text(player.name) },
                            supportingContent = { Text("Seat ${player.seat + 1}") }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onGameStart("session_${System.currentTimeMillis()}") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = true // enable when at least 1 player (or solo)
            ) {
                Text("Start Game")
            }
        }
    }
}
