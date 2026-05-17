package com.reznick.spitecards.data.model

enum class GameType(val displayName: String) {
    CASSINO("Cassino"),
    CRIBBAGE("Cribbage"),
    GARBAGE("Garbage"),
    FREEFORM("Free-form")
}

enum class CassinoVariant(val displayName: String) {
    STANDARD("Standard"),
    ROYAL("Royal Cassino"),
    SPADE("Spade Cassino"),
    CALIFORNIA("California Cassino")
}

enum class RulesStrictness(val displayName: String) {
    STRICT("Strict"),
    SOFT("Soft"),
    OFF("Off")
}

enum class UndoPolicy(val displayName: String) {
    OFF("No undos"),
    IMMEDIATE("Immediate (3 sec)"),
    CONSENT_REQUIRED("Consent required")
}

enum class DisconnectPolicy(val displayName: String) {
    PAUSE_AND_WAIT("Pause and wait"),
    HOST_SUBSTITUTES("Host substitutes"),
    END_GAME("End game")
}

enum class JoinMethod(val displayName: String) {
    OPEN("Open lobby"),
    CODE("4-digit code"),
    QR_ONLY("QR only")
}

enum class TableMode {
    HOST_PLAYS,
    HOST_PLAYS_SEPARATE_TABLE,
    HOST_TABLE_ONLY
}
