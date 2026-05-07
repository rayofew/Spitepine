package com.reznick.spitepine

import android.app.Application
import android.content.Context
import com.reznick.spitepine.data.auth.AuthRepository
import com.reznick.spitepine.data.repository.FirestoreTreeRepository
import com.reznick.spitepine.data.repository.TreeRepository

// Spec §13: "No DI framework in v1." Repository instances live on the
// Application; ViewModels reach them via Context.app.
class SpitePineApp : Application() {
    lateinit var treeRepository: TreeRepository
        private set

    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        authRepository = AuthRepository(this)
        treeRepository = FirestoreTreeRepository()
    }
}

val Context.app: SpitePineApp
    get() = applicationContext as SpitePineApp
