package com.reznick.spitecards.integration.spitescore

import kotlinx.serialization.Serializable

@Serializable
data class GameResultPayload(
    val schemaVersion: Int = SchemaVersion.CURRENT,
    val sourceApp: String = "spitecards",
    val gameType: String,
    val variant: String = "",
    val ruleOptions: Map<String, Boolean> = emptyMap(),
    val players: List<PayloadPlayer>,
    val scores: List<PayloadScore>,
    val winnerSeats: List<Int>,
    val playedAt: String,
    val durationSeconds: Long
)

@Serializable
data class PayloadPlayer(val name: String, val seat: Int)

@Serializable
data class PayloadScore(val seat: Int, val total: Int)
