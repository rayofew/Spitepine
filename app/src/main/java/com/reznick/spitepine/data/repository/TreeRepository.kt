package com.reznick.spitepine.data.repository

import com.reznick.spitepine.data.model.HarvestEntry
import com.reznick.spitepine.data.model.PermissionEvent
import com.reznick.spitepine.data.model.Tree
import kotlinx.coroutines.flow.Flow

// Per spec Appendix A.2. Result<T> at the boundary so ViewModels handle failure
// explicitly instead of throwing across UI suspension points.
interface TreeRepository {
    fun observeTrees(): Flow<List<Tree>>
    suspend fun addTree(tree: Tree): Result<String>
    suspend fun updateTree(treeId: String, updates: Map<String, Any?>): Result<Unit>
    suspend fun softDelete(treeId: String): Result<Unit>
    suspend fun appendPermissionEvent(treeId: String, event: PermissionEvent): Result<Unit>
    suspend fun appendHarvestEntry(treeId: String, entry: HarvestEntry): Result<Unit>
}
