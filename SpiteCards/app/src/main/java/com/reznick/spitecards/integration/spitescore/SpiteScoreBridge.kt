package com.reznick.spitecards.integration.spitescore

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.reznick.spitecards.data.model.GameResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.format.DateTimeFormatter

private const val SPITESCORE_PACKAGE = "com.reznick.spitescore"
private const val ACTION_RECEIVE_RESULT = "com.reznick.spite.action.RECEIVE_GAME_RESULT"
private const val ACTION_START_SESSION = "com.reznick.spite.action.START_GAME_SESSION"
private const val MIME_RESULT = "application/vnd.spite.gameresult+json"
private const val MIME_SETUP = "application/vnd.spite.gamesetup+json"
private const val EXTRA_PAYLOAD = "payload"

private val json = Json { ignoreUnknownKeys = true }

class SpiteScoreBridge(private val context: Context) {

    fun isSpiteScoreInstalled(): Boolean {
        val intent = Intent(ACTION_RECEIVE_RESULT).apply {
            setPackage(SPITESCORE_PACKAGE)
            type = MIME_RESULT
        }
        return context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
    }

    fun sendGameResult(result: GameResult): Result<Unit> = runCatching {
        val payload = result.toPayload()
        val intent = Intent(ACTION_RECEIVE_RESULT).apply {
            setPackage(SPITESCORE_PACKAGE)
            type = MIME_RESULT
            putExtra(EXTRA_PAYLOAD, json.encodeToString(payload))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun parseIncomingGameSetup(intent: Intent): Result<GameSetupPayload> = runCatching {
        val raw = intent.getStringExtra(EXTRA_PAYLOAD)
            ?: error("Missing payload extra in incoming intent.")
        val setup = json.decodeFromString<GameSetupPayload>(raw)
        if (!SchemaVersion.isCompatible(setup.schemaVersion)) {
            error(SchemaVersion.incompatibleMessage(setup.schemaVersion))
        }
        setup
    }

    fun openSpiteScoreInPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("market://details?id=$SPITESCORE_PACKAGE")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    private fun GameResult.toPayload() = GameResultPayload(
        gameType = gameType.name.lowercase(),
        variant = variant.lowercase(),
        ruleOptions = buildMap {
            put("shortGame", config.shortGame)
            put("muggins", config.muggins)
        },
        players = players.map { PayloadPlayer(it.name, it.seat) },
        scores = scores.map { (seat, total) -> PayloadScore(seat, total) },
        winnerSeats = winnerSeats,
        playedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(playedAt)),
        durationSeconds = durationSeconds
    )
}
