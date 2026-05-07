package com.reznick.spitepine.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

enum class TreeStatus {
    SPOTTED,
    ASKED,
    GRANTED,
    DENIED,
    EXPIRED,
    HARVESTED_RECENTLY,
}

data class Tree(
    val id: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val address: String = "",
    val status: TreeStatus = TreeStatus.SPOTTED,
    val notes: String = "",
    val photos: List<Photo> = emptyList(),
    val permissionHistory: List<PermissionEvent> = emptyList(),
    val harvestLog: List<HarvestEntry> = emptyList(),
    val treeDetails: TreeDetails = TreeDetails(),
    val createdAt: Timestamp? = null,
    val createdBy: String = "",
    val createdByName: String = "",
    val updatedAt: Timestamp? = null,
    val updatedBy: String = "",
    val deletedAt: Timestamp? = null,
)

data class TreeDetails(
    val sizeEstimate: String = "",
    val accessibilityNotes: String = "",
    val homeownerContact: String = "",
    val coneRipeningMonths: List<Int> = emptyList(),
)
