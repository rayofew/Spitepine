package com.reznick.spitepine.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.reznick.spitepine.data.model.HarvestEntry
import com.reznick.spitepine.data.model.PermissionEvent
import com.reznick.spitepine.data.model.Tree
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

// Spec §7. Firestore SDK keeps a local cache (default-on for Android) so
// snapshot listeners fire immediately on local writes; we don't await server
// ACKs on the write path — the listener drives UI either way (§12).
class FirestoreTreeRepository : TreeRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth
    private val trees get() = db.collection(COLLECTION)

    override fun observeTrees(): Flow<List<Tree>> = callbackFlow {
        val registration = trees
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                if (snap == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap.documents.mapNotNull { doc ->
                    runCatching { doc.toObject(Tree::class.java) }
                        .getOrNull()
                        ?.copy(id = doc.id)
                }.filter { it.deletedAt == null }
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addTree(tree: Tree): Result<String> = runCatching {
        val user = checkNotNull(auth.currentUser) { "Not signed in" }
        val id = tree.id.ifBlank { UUID.randomUUID().toString() }
        val data = mapOf(
            "id" to id,
            "location" to tree.location,
            "address" to tree.address,
            "status" to tree.status.name,
            "notes" to tree.notes,
            "photos" to tree.photos,
            "permissionHistory" to tree.permissionHistory,
            "harvestLog" to tree.harvestLog,
            "treeDetails" to tree.treeDetails,
            "createdAt" to FieldValue.serverTimestamp(),
            "createdBy" to user.uid,
            "createdByName" to (user.displayName ?: ""),
            "updatedAt" to FieldValue.serverTimestamp(),
            "updatedBy" to user.uid,
            "deletedAt" to null,
        )
        trees.document(id).set(data) // local cache write is sync; server sync async
        id
    }

    override suspend fun updateTree(
        treeId: String,
        updates: Map<String, Any?>
    ): Result<Unit> = runCatching {
        val user = checkNotNull(auth.currentUser) { "Not signed in" }
        val withMeta = updates + mapOf(
            "updatedAt" to FieldValue.serverTimestamp(),
            "updatedBy" to user.uid,
        )
        trees.document(treeId).update(withMeta)
        Unit
    }

    override suspend fun softDelete(treeId: String): Result<Unit> = runCatching {
        val user = checkNotNull(auth.currentUser) { "Not signed in" }
        trees.document(treeId).update(
            mapOf(
                "deletedAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "updatedBy" to user.uid,
            )
        )
        Unit
    }

    override suspend fun appendPermissionEvent(
        treeId: String,
        event: PermissionEvent,
    ): Result<Unit> = runCatching {
        val user = checkNotNull(auth.currentUser) { "Not signed in" }
        val eventMap = mapOf(
            "timestamp" to (event.timestamp ?: Timestamp.now()),
            "status" to event.status.name,
            "spokeWith" to event.spokeWith,
            "agreementNotes" to event.agreementNotes,
            "byUserId" to event.byUserId.ifBlank { user.uid },
            "byUserName" to event.byUserName.ifBlank { user.displayName.orEmpty() },
        )
        trees.document(treeId).update(
            mapOf(
                "permissionHistory" to FieldValue.arrayUnion(eventMap),
                "status" to event.status.name,
                "updatedAt" to FieldValue.serverTimestamp(),
                "updatedBy" to user.uid,
            )
        )
        Unit
    }

    override suspend fun appendHarvestEntry(
        treeId: String,
        entry: HarvestEntry,
    ): Result<Unit> = runCatching {
        val user = checkNotNull(auth.currentUser) { "Not signed in" }
        val id = entry.id.ifBlank { UUID.randomUUID().toString() }
        val entryMap = mapOf(
            "id" to id,
            "timestamp" to (entry.timestamp ?: Timestamp.now()),
            "yieldEstimate" to entry.yieldEstimate,
            "notes" to entry.notes,
            "photoIds" to entry.photoIds,
            "byUserId" to entry.byUserId.ifBlank { user.uid },
            "byUserName" to entry.byUserName.ifBlank { user.displayName.orEmpty() },
        )
        trees.document(treeId).update(
            mapOf(
                "harvestLog" to FieldValue.arrayUnion(entryMap),
                "updatedAt" to FieldValue.serverTimestamp(),
                "updatedBy" to user.uid,
            )
        )
        Unit
    }

    private companion object {
        const val COLLECTION = "trees"
    }
}
