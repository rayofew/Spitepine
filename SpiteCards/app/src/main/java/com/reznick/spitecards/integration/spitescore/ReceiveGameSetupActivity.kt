package com.reznick.spitecards.integration.spitescore

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.reznick.spitecards.MainActivity

class ReceiveGameSetupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bridge = SpiteScoreBridge(applicationContext)
        bridge.parseIncomingGameSetup(intent)
            .onSuccess { setup ->
                val mainIntent = Intent(this, MainActivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    putExtra(MainActivity.EXTRA_GAME_SETUP, bridge.let {
                        kotlinx.serialization.json.Json.encodeToString(GameSetupPayload.serializer(), setup)
                    })
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(mainIntent)
            }
            .onFailure {
                // Incompatible schema or missing payload — open the app normally
                val mainIntent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(mainIntent)
            }
        finish()
    }
}
