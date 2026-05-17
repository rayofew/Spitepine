package com.reznick.spitecards.ui.history

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
import com.reznick.spitecards.data.db.entities.GameSessionEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHistoryScreen(onBack: () -> Unit) {
    // In production: collected from GameHistoryViewModel
    val sessions = remember { emptyList<GameSessionEntity>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No games yet. Host or join one to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(sessions) { session ->
                    SessionRow(session)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: GameSessionEntity) {
    val date = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        .format(Date(session.playedAt))
    ListItem(
        headlineContent = { Text(session.gameType.lowercase().replaceFirstChar { it.uppercase() }) },
        supportingContent = { Text(date) },
        trailingContent = { Text(session.winnerSeats) }
    )
}
