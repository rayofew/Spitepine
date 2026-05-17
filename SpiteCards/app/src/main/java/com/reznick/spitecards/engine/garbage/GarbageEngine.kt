package com.reznick.spitecards.engine.garbage

import com.reznick.spitecards.data.model.*
import com.reznick.spitecards.engine.*

private const val PHASE_DRAW = "DRAW"
private const val PHASE_PLACE = "PLACE"

class GarbageEngine : GameEngine {

    override fun initialState(players: List<Player>, config: GameConfig): GameState {
        val activePlayers = players.filter { !it.isTableOnly }
        val deck = STANDARD_DECK.shuffled().toMutableList()
        // Each player gets a face-down 10-slot layout; we encode it as their "hand"
        val hands = activePlayers.associate { player ->
            player.seat to deck.take(10).also { repeat(10) { deck.removeFirst() } }
        }
        val slotCounts = activePlayers.associate { "slots_${it.seat}" to "10" }
        return GameState(
            gameType = GameType.GARBAGE,
            variant = "standard",
            phase = PHASE_DRAW,
            deck = deck,
            hands = hands,
            tableCards = emptyList(),
            scores = activePlayers.associate { it.seat to 0 },
            currentTurn = activePlayers.first().seat,
            gameData = slotCounts.toMutableMap()
        )
    }

    override fun applyAction(state: GameState, action: GameAction, strictness: RulesStrictness): ActionResult {
        if (action.playerSeat != state.currentTurn) return ActionResult.Illegal("Not your turn.")
        return when {
            state.phase == PHASE_DRAW && action is GameAction.DrawCard -> handleDraw(state, action)
            state.phase == PHASE_PLACE && action is GameAction.PlaceInSlot -> handlePlace(state, action)
            state.phase == PHASE_PLACE && action is GameAction.DiscardCard -> handleDiscard(state, action)
            else -> ActionResult.Illegal("Invalid action for phase ${state.phase}.")
        }
    }

    private fun handleDraw(state: GameState, action: GameAction.DrawCard): ActionResult {
        val drawn = if (action.fromDiscard) {
            state.discardPile.lastOrNull() ?: return ActionResult.Illegal("Discard pile is empty.")
        } else {
            state.deck.firstOrNull() ?: return ActionResult.Illegal("Deck is empty.")
        }
        val newDeck = if (action.fromDiscard) state.deck else state.deck.drop(1)
        val newDiscard = if (action.fromDiscard) state.discardPile.dropLast(1) else state.discardPile
        val newData = state.gameData.toMutableMap().also { it["drawn_card"] = drawn.id }
        return ActionResult.Success(state.copy(deck = newDeck, discardPile = newDiscard, gameData = newData, phase = PHASE_PLACE))
    }

    private fun handlePlace(state: GameState, action: GameAction.PlaceInSlot): ActionResult {
        val drawnCardId = state.gameData["drawn_card"] ?: return ActionResult.Illegal("No card drawn yet.")
        val drawnCard = STANDARD_DECK.find { it.id == drawnCardId } ?: return ActionResult.Illegal("Drawn card not found.")
        val layout = state.hands[action.playerSeat]?.toMutableList() ?: return ActionResult.Illegal("No layout.")

        // Slot index is 0-based; valid placement is when card rank matches slot+1
        val targetSlot = action.slotIndex
        val isValidSlot = drawnCard.rank.pip == targetSlot + 1 && drawnCard.rank != Rank.JACK && drawnCard.rank != Rank.QUEEN && drawnCard.rank != Rank.KING
        if (!isValidSlot) {
            return ActionResult.Illegal("${drawnCard.rank.display} doesn't go in slot ${targetSlot + 1}.")
        }
        val displaced = layout[targetSlot]
        layout[targetSlot] = drawnCard
        val newHands = state.hands.toMutableMap().also { it[action.playerSeat] = layout }
        val newData = state.gameData.toMutableMap().also { it["drawn_card"] = displaced.id }

        // If all slots filled, this player wins the round
        val allFilled = layout.all { isRevealed(it) }
        return if (allFilled) {
            val newSlots = state.gameData.toMutableMap()
            val currentSlots = newSlots["slots_${action.playerSeat}"]?.toIntOrNull() ?: 10
            newSlots["slots_${action.playerSeat}"] = (currentSlots - 1).toString()
            if (currentSlots - 1 == 0) {
                ActionResult.Success(state.copy(hands = newHands, gameData = newSlots, isOver = true, winnerSeats = listOf(action.playerSeat)))
            } else {
                ActionResult.Success(state.copy(hands = newHands, gameData = newSlots, phase = PHASE_DRAW, currentTurn = nextTurn(state)))
            }
        } else {
            // Displaced card becomes the new drawn card; player can place it or discard
            ActionResult.Success(state.copy(hands = newHands, gameData = newData))
        }
    }

    private fun isRevealed(card: Card): Boolean = card.rank.pip in 1..10

    private fun handleDiscard(state: GameState, action: GameAction.DiscardCard): ActionResult {
        val drawnCardId = state.gameData["drawn_card"] ?: return ActionResult.Illegal("No card drawn.")
        val drawnCard = STANDARD_DECK.find { it.id == drawnCardId } ?: return ActionResult.Illegal("Drawn card not found.")
        val newDiscard = state.discardPile + drawnCard
        val newData = state.gameData.toMutableMap().also { it.remove("drawn_card") }
        return ActionResult.Success(state.copy(discardPile = newDiscard, gameData = newData, phase = PHASE_DRAW, currentTurn = nextTurn(state)))
    }

    private fun nextTurn(state: GameState): Int {
        val seats = state.hands.keys.sorted()
        return seats.firstOrNull { it > state.currentTurn } ?: seats.first()
    }

    override fun isGameOver(state: GameState) = state.isOver
    override fun scores(state: GameState) = state.scores
    override fun winnerSeats(state: GameState) = state.winnerSeats
}
