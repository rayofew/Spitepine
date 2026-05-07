package com.reznick.spitepine

import android.app.Application
import android.content.Context
import com.reznick.spitepine.data.repository.InMemoryTreeRepository
import com.reznick.spitepine.data.repository.TreeRepository

// Spec §13: "No DI framework in v1." Repository instances live on the
// Application; ViewModels reach them via Context.app.
class SpitePineApp : Application() {
    lateinit var treeRepository: TreeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        // TODO Phase 2 chunk B: swap to a FirestoreTreeRepository once
        // sign-in lands and the Firestore rules' UID list is populated.
        treeRepository = InMemoryTreeRepository()
    }
}

val Context.app: SpitePineApp
    get() = applicationContext as SpitePineApp
