package com.reznick.spitecards.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("SpiteCards", style = MaterialTheme.typography.headlineLarge)
            Text(
                "A virtual deck of cards that links phones in the same room. " +
                "Free, ad-free, telemetry-free, out of spite.",
                style = MaterialTheme.typography.bodyLarge
            )

            HorizontalDivider()

            Text("Why it exists", style = MaterialTheme.typography.titleLarge)
            Text(
                "Physical cards aren't always available. Existing apps are ad-ridden, " +
                "account-locked, or quietly tracking. SpiteCards is what you wish existed.",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            Text("Privacy", style = MaterialTheme.typography.titleLarge)
            Text(
                "No analytics. No crash reporting. No Firebase. No nothing. " +
                "All gameplay data is stored locally on your device. " +
                "All communication is between the phones in your room — no server, no internet required.",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            Text("The Spite line", style = MaterialTheme.typography.titleLarge)
            Text(
                "SpiteCards is part of a small family of apps that share the same ethos: " +
                "free, no ads, no tracking, no accounts. Same answer to every product decision: " +
                "because the ad-driven apps wouldn't bother.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text("Other apps in the Spite line:", style = MaterialTheme.typography.bodyMedium)
            Text("· SpiteScore — track scores for any game", style = MaterialTheme.typography.bodyMedium)
            Text("· SpitePine — map and track your pine trees", style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider()

            Text(
                "Version 1.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
