package com.reznick.spitecards.data.model

data class GameConfig(
    val gameType: GameType,
    val variant: String = "",
    val rulesStrictness: RulesStrictness = RulesStrictness.SOFT,
    val undoPolicy: UndoPolicy = UndoPolicy.CONSENT_REQUIRED,
    val disconnectPolicy: DisconnectPolicy = DisconnectPolicy.PAUSE_AND_WAIT,
    val joinMethod: JoinMethod = JoinMethod.OPEN,
    val tableMode: TableMode = TableMode.HOST_PLAYS,
    val joinCode: String? = null,
    // Cribbage-specific
    val shortGame: Boolean = false,
    val muggins: Boolean = false,
    // Cassino-specific
    val cassinoVariant: CassinoVariant = CassinoVariant.STANDARD,
    // Freeform-specific
    val deckJokers: Int = 0,
    val deckCopies: Int = 1,
    val dealCount: Int = 5
)
