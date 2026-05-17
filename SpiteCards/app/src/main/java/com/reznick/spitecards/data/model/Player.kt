package com.reznick.spitecards.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
    val seat: Int,
    val isHost: Boolean = false,
    val isTableOnly: Boolean = false
)
