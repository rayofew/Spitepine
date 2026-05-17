package com.reznick.spitecards.engine

import kotlinx.serialization.Serializable

@Serializable
sealed class GameAction {
    abstract val playerSeat: Int

    @Serializable
    data class PlayCard(
        override val playerSeat: Int,
        val cardId: String,
        val targetCardIds: List<String> = emptyList(),
        val targetRegion: String = ""
    ) : GameAction()

    @Serializable
    data class DrawCard(
        override val playerSeat: Int,
        val fromDiscard: Boolean = false
    ) : GameAction()

    @Serializable
    data class PlaceInSlot(
        override val playerSeat: Int,
        val cardId: String,
        val slotIndex: Int
    ) : GameAction()

    @Serializable
    data class DiscardCard(
        override val playerSeat: Int,
        val cardId: String
    ) : GameAction()

    @Serializable
    data class RequestUndo(
        override val playerSeat: Int,
        val description: String = ""
    ) : GameAction()

    @Serializable
    data class VoteUndo(
        override val playerSeat: Int,
        val approved: Boolean
    ) : GameAction()

    @Serializable
    data class EndTurn(
        override val playerSeat: Int
    ) : GameAction()

    @Serializable
    data class FlipStarterCard(
        override val playerSeat: Int
    ) : GameAction()
}
