package com.reznick.spitepine.data.model

import com.google.firebase.Timestamp

data class PermissionEvent(
    val timestamp: Timestamp? = null,
    val status: TreeStatus = TreeStatus.SPOTTED,
    val spokeWith: String = "",
    val agreementNotes: String = "",
    val byUserId: String = "",
    val byUserName: String = "",
)
