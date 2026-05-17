package com.reznick.spitecards.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reznick.spitecards.data.model.Card
import com.reznick.spitecards.ui.components.CardView

@Composable
fun GameScreen(
    sessionId: String,
    onGameEnd: () -> Unit
) {
    // In production, these come from GameViewModel observing NearbyConnectionsManager
    val hand = remember { mutableStateListOf<Card>() }
    val tableCards = remember { mutableStateListOf<Card>() }
    var selectedCardId by remember { mutableStateOf<String?>(null) }
    var isYourTurn by remember { mutableStateOf(true) }
    var turnLabel by remember { mutableStateOf("Your turn — play a card") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Table area (top)
            TableArea(
                tableCards = tableCards,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(8.dp)
            )

            // Turn indicator
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp
            ) {
                Text(
                    text = turnLabel,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                    color = if (isYourTurn) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(Modifier.weight(0.05f))

            // Player hand (bottom)
            PlayerHand(
                hand = hand,
                selectedCardId = selectedCardId,
                onCardTap = { card ->
                    selectedCardId = if (selectedCardId == card.id) null else card.id
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* play selected card */ },
                    enabled = selectedCardId != null && isYourTurn,
                    modifier = Modifier.weight(1f)
                ) { Text("Play") }

                OutlinedButton(
                    onClick = onGameEnd,
                    modifier = Modifier.weight(1f)
                ) { Text("End Game") }
            }
        }
    }
}

@Composable
private fun TableArea(tableCards: List<Card>, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, tonalElevation = 1.dp, shape = MaterialTheme.shapes.medium) {
        if (tableCards.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Table is empty", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy((-30).dp)
            ) {
                items(tableCards) { card ->
                    CardView(card = card, width = 56.dp, height = 80.dp)
                }
            }
        }
    }
}

@Composable
private fun PlayerHand(
    hand: List<Card>,
    selectedCardId: String?,
    onCardTap: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, tonalElevation = 1.dp, shape = MaterialTheme.shapes.medium) {
        if (hand.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No cards in hand", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy((-24).dp)
            ) {
                items(hand) { card ->
                    CardView(
                        card = card,
                        selected = card.id == selectedCardId,
                        onClick = { onCardTap(card) },
                        width = 56.dp,
                        height = 80.dp
                    )
                }
            }
        }
    }
}
