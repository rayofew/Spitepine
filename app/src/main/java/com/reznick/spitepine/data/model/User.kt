package com.reznick.spitepine.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val colorHex: String = "",
    val addedToTeamAt: Timestamp? = null,
)
