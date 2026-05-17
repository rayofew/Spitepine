package com.reznick.spitecards.data.model

import kotlinx.serialization.Serializable

enum class Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

enum class Rank(val pip: Int, val display: String) {
    ACE(1, "A"),
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K"),
    JOKER(0, "★")
}

@Serializable
data class Card(
    val rank: Rank,
    val suit: Suit,
    val id: String = "${rank.name}_${suit.name}"
)

val STANDARD_DECK: List<Card> = Suit.entries.flatMap { suit ->
    Rank.entries.filter { it != Rank.JOKER }.map { rank -> Card(rank, suit) }
}

fun standardDeckWith(jokers: Int = 0, copies: Int = 1): List<Card> {
    val base = (1..copies).flatMap { STANDARD_DECK }
    val jokerCards = (1..jokers).map { Card(Rank.JOKER, Suit.CLUBS, "JOKER_$it") }
    return base + jokerCards
}
