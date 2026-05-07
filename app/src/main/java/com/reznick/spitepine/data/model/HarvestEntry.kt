package com.reznick.spitepine.data.model

import com.google.firebase.Timestamp

data class HarvestEntry(
    val id: String = "",
    val timestamp: Timestamp? = null,
    val yieldEstimate: String = "",
    val notes: String = "",
    val photoIds: List<String> = emptyList(),
    val byUserId: String = "",
    val byUserName: String = "",
)
