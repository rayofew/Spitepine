package com.reznick.spitecards.nearby

import com.reznick.spitecards.data.model.Player
import com.reznick.spitecards.engine.GameAction
import com.reznick.spitecards.engine.GameState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
sealed class GameMessage {

    @Serializable
    data class PlayerJoined(val player: Player) : GameMessage()

    @Serializable
    data class PlayerLeft(val playerSeat: Int) : GameMessage()

    @Serializable
    data class LobbyUpdate(val players: List<Player>) : GameMessage()

    @Serializable
    data class GameStarted(val initialState: GameState) : GameMessage()

    @Serializable
    data class ActionSubmitted(val action: GameAction) : GameMessage()

    @Serializable
    data class StateUpdate(val state: GameState) : GameMessage()

    @Serializable
    data class UndoRequest(val playerSeat: Int, val description: String, val timeoutSeconds: Int = 10) : GameMessage()

    @Serializable
    data class UndoVote(val playerSeat: Int, val approved: Boolean) : GameMessage()

    @Serializable
    data class UndoResolved(val approved: Boolean) : GameMessage()

    @Serializable
    data class GameEnded(val finalState: GameState) : GameMessage()

    @Serializable
    data class ChatPing(val timestamp: Long = System.currentTimeMillis()) : GameMessage()
}

private val json = Json { ignoreUnknownKeys = true; classDiscriminator = "type" }

fun GameMessage.encode(): ByteArray = json.encodeToString(this).toByteArray(Charsets.UTF_8)
fun ByteArray.decodeMessage(): GameMessage = json.decodeFromString(toString(Charsets.UTF_8))
