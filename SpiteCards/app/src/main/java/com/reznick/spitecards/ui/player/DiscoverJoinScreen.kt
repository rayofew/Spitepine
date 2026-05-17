package com.reznick.spitecards.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reznick.spitecards.nearby.DiscoveredEndpoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverJoinScreen(
    onJoined: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var code by remember { mutableStateOf("") }
    val nearby = remember { mutableStateListOf<DiscoveredEndpoint>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join a Game") },
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
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Nearby") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Code") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Scan QR") })
            }

            when (selectedTab) {
                0 -> NearbyTab(nearby, onJoined)
                1 -> CodeTab(code, onCodeChange = { code = it }, onJoin = { onJoined("session_code") })
                2 -> QrTab(onScanned = { onJoined("session_qr") })
            }
        }
    }
}

@Composable
private fun NearbyTab(endpoints: List<DiscoveredEndpoint>, onJoin: (String) -> Unit) {
    if (endpoints.isEmpty()) {
        Box(modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(32.dp)) {
            Text(
                "Searching for nearby games…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn {
            items(endpoints) { ep ->
                ListItem(
                    headlineContent = { Text(ep.name) },
                    trailingContent = {
                        TextButton(onClick = { onJoin(ep.id) }) { Text("Join") }
                    }
                )
            }
        }
    }
}

@Composable
private fun CodeTab(code: String, onCodeChange: (String) -> Unit, onJoin: () -> Unit) {
    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Enter the 4-digit code shown on the host's screen.", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) onCodeChange(it) },
            label = { Text("Join code") },
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(onClick = onJoin, enabled = code.length == 4, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
            Text("Join")
        }
    }
}

@Composable
private fun QrTab(onScanned: (String) -> Unit) {
    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(32.dp)) {
        Text(
            "Camera opens here to scan the host's QR code.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
