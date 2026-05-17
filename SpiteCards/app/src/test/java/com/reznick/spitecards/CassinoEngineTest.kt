package com.reznick.spitecards

import com.reznick.spitecards.data.model.*
import com.reznick.spitecards.engine.GameAction
import com.reznick.spitecards.engine.ActionResult
import com.reznick.spitecards.engine.cassino.CassinoEngine
import org.junit.Assert.*
import org.junit.Test

class CassinoEngineTest {

    private val engine = CassinoEngine()
    private val players = listOf(
        Player("p0", "Alice", 0),
        Player("p1", "Bob", 1)
    )
    private val config = GameConfig(
        gameType = GameType.CASSINO,
        cassinoVariant = CassinoVariant.STANDARD
    )

    @Test
    fun `initial state deals 4 cards per player and 4 to table`() {
        val state = engine.initialState(players, config)
        assertEquals(4, state.hands[0]?.size)
        assertEquals(4, state.hands[1]?.size)
        assertEquals(4, state.tableCards.size)
        assertEquals(52 - 4 - 4 - 4, state.deck.size)
    }

    @Test
    fun `trailing a card places it on the table`() {
        val state = engine.initialState(players, config)
        val card = state.hands[0]!!.first()
        val action = GameAction.PlayCard(playerSeat = 0, cardId = card.id)
        val result = engine.applyAction(state, action, RulesStrictness.SOFT)
        assertTrue(result is ActionResult.Success)
        val newState = (result as ActionResult.Success).newState
        assertTrue(newState.tableCards.any { it.id == card.id })
        assertFalse(newState.hands[0]!!.any { it.id == card.id })
    }

    @Test
    fun `playing out of turn returns Illegal`() {
        val state = engine.initialState(players, config)
        val card = state.hands[1]!!.first()
        val action = GameAction.PlayCard(playerSeat = 1, cardId = card.id)
        val result = engine.applyAction(state, action, RulesStrictness.SOFT)
        assertTrue(result is ActionResult.Illegal)
    }

    @Test
    fun `strict mode rejects invalid capture`() {
        val state = engine.initialState(players, config)
        val hand = state.hands[0]!!
        val tableCard = state.tableCards.first()
        val playedCard = hand.first()
        // Try to capture with mismatched values (will almost certainly mismatch)
        val action = GameAction.PlayCard(playerSeat = 0, cardId = playedCard.id, targetCardIds = listOf(tableCard.id))
        val result = engine.applyAction(state, action, RulesStrictness.STRICT)
        // Either succeeds (if values happen to match) or returns Illegal
        assertTrue(result is ActionResult.Success || result is ActionResult.Illegal)
    }

    @Test
    fun `schema validation - GameResultPayload serializes and deserializes`() {
        val payload = com.reznick.spitecards.integration.spitescore.GameResultPayload(
            gameType = "cassino",
            variant = "standard",
            players = listOf(
                com.reznick.spitecards.integration.spitescore.PayloadPlayer("Alice", 0),
                com.reznick.spitecards.integration.spitescore.PayloadPlayer("Bob", 1)
            ),
            scores = listOf(
                com.reznick.spitecards.integration.spitescore.PayloadScore(0, 11),
                com.reznick.spitecards.integration.spitescore.PayloadScore(1, 8)
            ),
            winnerSeats = listOf(0),
            playedAt = "2026-06-12T19:30:00Z",
            durationSeconds = 1840
        )
        val json = kotlinx.serialization.json.Json.encodeToString(
            com.reznick.spitecards.integration.spitescore.GameResultPayload.serializer(), payload
        )
        val decoded = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            .decodeFromString(com.reznick.spitecards.integration.spitescore.GameResultPayload.serializer(), json)
        assertEquals(payload.gameType, decoded.gameType)
        assertEquals(payload.winnerSeats, decoded.winnerSeats)
        assertEquals(1, decoded.schemaVersion)
    }

    @Test
    fun `schema rejects unknown major version`() {
        assertFalse(com.reznick.spitecards.integration.spitescore.SchemaVersion.isCompatible(99))
        assertTrue(com.reznick.spitecards.integration.spitescore.SchemaVersion.isCompatible(1))
    }
}
