package com.reznick.spitecards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.reznick.spitecards.integration.spitescore.GameSetupPayload
import com.reznick.spitecards.navigation.SpiteCardsNavHost
import com.reznick.spitecards.ui.theme.SpiteCardsTheme
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_GAME_SETUP = "extra_game_setup"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val incomingSetup: GameSetupPayload? = intent.getStringExtra(EXTRA_GAME_SETUP)?.let {
            runCatching { Json.decodeFromString<GameSetupPayload>(it) }.getOrNull()
        }
        setContent {
            SpiteCardsTheme {
                SpiteCardsNavHost(incomingSetup = incomingSetup)
            }
        }
    }
}
