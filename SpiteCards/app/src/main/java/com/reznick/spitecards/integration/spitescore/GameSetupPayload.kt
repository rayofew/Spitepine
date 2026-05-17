package com.reznick.spitecards.integration.spitescore

import kotlinx.serialization.Serializable

@Serializable
data class GameSetupPayload(
    val schemaVersion: Int,
    val sourceApp: String,
    val gameType: String,
    val variant: String = "",
    val ruleOptions: Map<String, Boolean> = emptyMap(),
    val players: List<SetupPlayer>
)

@Serializable
data class SetupPlayer(val name: String)
