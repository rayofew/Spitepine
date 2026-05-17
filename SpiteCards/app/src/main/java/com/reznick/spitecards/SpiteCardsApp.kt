package com.reznick.spitecards

import android.app.Application
import com.reznick.spitecards.data.db.SpiteCardsDatabase
import com.reznick.spitecards.data.repository.GameHistoryRepository
import com.reznick.spitecards.integration.spitescore.SpiteScoreBridge

class SpiteCardsApp : Application() {

    val database by lazy { SpiteCardsDatabase.get(this) }
    val gameHistoryRepository by lazy { GameHistoryRepository(database.gameSessionDao()) }
    val spiteScoreBridge by lazy { SpiteScoreBridge(this) }
}
