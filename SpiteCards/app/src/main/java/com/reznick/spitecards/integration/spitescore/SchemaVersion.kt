package com.reznick.spitecards.integration.spitescore

object SchemaVersion {
    const val CURRENT = 1

    fun isCompatible(version: Int): Boolean = version == CURRENT

    fun incompatibleMessage(version: Int): String =
        "This game came from a newer version of SpiteScore (schema v$version). Update SpiteCards to open it."
}
