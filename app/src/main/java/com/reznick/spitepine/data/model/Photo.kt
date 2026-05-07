package com.reznick.spitepine.data.model

import com.google.firebase.Timestamp

data class Photo(
    val id: String = "",
    val thumbnailPath: String = "",
    val displayPath: String = "",
    val uploadedAt: Timestamp? = null,
    val uploadedBy: String = "",
)
