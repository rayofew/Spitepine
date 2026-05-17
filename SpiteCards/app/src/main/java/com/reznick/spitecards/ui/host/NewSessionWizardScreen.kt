package com.reznick.spitecards.ui.host

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reznick.spitecards.data.model.*
import com.reznick.spitecards.integration.spitescore.GameSetupPayload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSessionWizardScreen(
    preloadedSetup: GameSetupPayload? = null,
    onSessionCreated: (String) -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedGame by remember { mutableStateOf(GameType.CASSINO) }
    var strictness by remember { mutableStateOf(RulesStrictness.SOFT) }
    var undoPolicy by remember { mutableStateOf(UndoPolicy.CONSENT_REQUIRED) }
    var joinMethod by remember { mutableStateOf(JoinMethod.OPEN) }

    // Pre-fill from SpiteScore handoff
    LaunchedEffect(preloadedSetup) {
        if (preloadedSetup != null) {
            selectedGame = GameType.entries.firstOrNull {
                it.name.lowercase() == preloadedSetup.gameType
            } ?: GameType.CASSINO
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Game — Step ${step + 1} of 4") },
                navigationIcon = {
                    IconButton(onClick = { if (step == 0) onBack() else step-- }) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                0 -> GamePickerStep(selectedGame) { selectedGame = it }
                1 -> RulesStep(strictness, undoPolicy, { strictness = it }, { undoPolicy = it })
                2 -> JoinMethodStep(joinMethod) { joinMethod = it }
                3 -> ConfirmStep(selectedGame, strictness, undoPolicy, joinMethod)
            }

            Spacer(Modifier.weight(1f))

            if (step < 3) {
                Button(onClick = { step++ }, modifier = Modifier.fillMaxWidth()) {
                    Text("Next")
                }
            } else {
                Button(
                    onClick = { onSessionCreated("session_${System.currentTimeMillis()}") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Hosting")
                }
            }

            TextButton(onClick = { onSessionCreated("session_${System.currentTimeMillis()}") }, modifier = Modifier.fillMaxWidth()) {
                Text("Quick Deal — skip setup")
            }
        }
    }
}

@Composable
private fun GamePickerStep(selected: GameType, onSelect: (GameType) -> Unit) {
    Text("Choose a game", style = MaterialTheme.typography.headlineMedium)
    GameType.entries.forEach { game ->
        Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
            RadioButton(selected = selected == game, onClick = { onSelect(game) })
            Spacer(Modifier.width(8.dp))
            Text(game.displayName, modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
        }
    }
}

@Composable
private fun RulesStep(
    strictness: RulesStrictness,
    undoPolicy: UndoPolicy,
    onStrictness: (RulesStrictness) -> Unit,
    onUndo: (UndoPolicy) -> Unit
) {
    Text("Rules", style = MaterialTheme.typography.headlineMedium)
    Text("Strictness", style = MaterialTheme.typography.titleLarge)
    RulesStrictness.entries.forEach { s ->
        Row {
            RadioButton(selected = strictness == s, onClick = { onStrictness(s) })
            Spacer(Modifier.width(8.dp))
            Text(s.displayName, modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
        }
    }
    Spacer(Modifier.height(16.dp))
    Text("Undo policy", style = MaterialTheme.typography.titleLarge)
    UndoPolicy.entries.forEach { p ->
        Row {
            RadioButton(selected = undoPolicy == p, onClick = { onUndo(p) })
            Spacer(Modifier.width(8.dp))
            Text(p.displayName, modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
        }
    }
}

@Composable
private fun JoinMethodStep(selected: JoinMethod, onSelect: (JoinMethod) -> Unit) {
    Text("How do players join?", style = MaterialTheme.typography.headlineMedium)
    JoinMethod.entries.forEach { method ->
        Row {
            RadioButton(selected = selected == method, onClick = { onSelect(method) })
            Spacer(Modifier.width(8.dp))
            Text(method.displayName, modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
        }
    }
}

@Composable
private fun ConfirmStep(game: GameType, strictness: RulesStrictness, undoPolicy: UndoPolicy, joinMethod: JoinMethod) {
    Text("Confirm setup", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(8.dp))
    Text("Game: ${game.displayName}")
    Text("Rules: ${strictness.displayName}")
    Text("Undo: ${undoPolicy.displayName}")
    Text("Join: ${joinMethod.displayName}")
}
