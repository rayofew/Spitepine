package com.reznick.spitepine.data.repository

import com.google.firebase.Timestamp
import com.reznick.spitepine.data.model.HarvestEntry
import com.reznick.spitepine.data.model.PermissionEvent
import com.reznick.spitepine.data.model.Tree
import com.reznick.spitepine.data.model.TreeStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID

// Stand-in repo until Firebase Auth is functional and Firestore reads/writes
// can be authorized. Same interface as the future FirestoreTreeRepository, so
// only the wiring on Application changes when we swap.
class InMemoryTreeRepository : TreeRepository {
    private val state = MutableStateFlow<List<Tree>>(emptyList())

    override fun observeTrees(): Flow<List<Tree>> =
        state.map { trees -> trees.filter { it.deletedAt == null } }

    override suspend fun addTree(tree: Tree): Result<String> {
        val id = tree.id.ifBlank { UUID.randomUUID().toString() }
        val now = Timestamp.now()
        val withMeta = tree.copy(
            id = id,
            createdAt = tree.createdAt ?: now,
            updatedAt = now,
        )
        state.update { it + withMeta }
        return Result.success(id)
    }

    override suspend fun updateTree(treeId: String, updates: Map<String, Any?>): Result<Unit> {
        state.update { trees ->
            trees.map { tree ->
                if (tree.id != treeId) tree else applyUpdates(tree, updates)
            }
        }
        return Result.success(Unit)
    }

    override suspend fun softDelete(treeId: String): Result<Unit> {
        state.update { trees ->
            trees.map {
                if (it.id == treeId) it.copy(deletedAt = Timestamp.now()) else it
            }
        }
        return Result.success(Unit)
    }

    override suspend fun appendPermissionEvent(
        treeId: String,
        event: PermissionEvent
    ): Result<Unit> {
        state.update { trees ->
            trees.map {
                if (it.id != treeId) it else it.copy(
                    permissionHistory = it.permissionHistory + event,
                    status = event.status,
                    updatedAt = Timestamp.now(),
                )
            }
        }
        return Result.success(Unit)
    }

    override suspend fun appendHarvestEntry(
        treeId: String,
        entry: HarvestEntry
    ): Result<Unit> {
        state.update { trees ->
            trees.map {
                if (it.id != treeId) it else it.copy(
                    harvestLog = it.harvestLog + entry,
                    updatedAt = Timestamp.now(),
                )
            }
        }
        return Result.success(Unit)
    }

    // Spec's update API uses a Firestore-style Map<String, Any?>. In-memory only
    // recognizes the small set of top-level scalar fields the current UI writes.
    // Unknown keys are ignored — the Firestore impl will respect them all.
    private fun applyUpdates(tree: Tree, updates: Map<String, Any?>): Tree {
        var t = tree
        for ((k, v) in updates) when (k) {
            "address" -> t = t.copy(address = (v as? String).orEmpty())
            "notes" -> t = t.copy(notes = (v as? String).orEmpty())
            "status" -> t = t.copy(status = v as? TreeStatus ?: t.status)
        }
        return t.copy(updatedAt = Timestamp.now())
    }
}
