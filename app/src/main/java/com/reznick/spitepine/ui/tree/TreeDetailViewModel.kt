package com.reznick.spitepine.ui.tree

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Timestamp
import com.reznick.spitepine.SpitePineApp
import com.reznick.spitepine.data.model.HarvestEntry
import com.reznick.spitepine.data.model.PermissionEvent
import com.reznick.spitepine.data.model.Tree
import com.reznick.spitepine.data.model.TreeStatus
import com.reznick.spitepine.data.repository.TreeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TreeDetailViewModel(
    private val repo: TreeRepository,
    private val treeId: String,
) : ViewModel() {

    val tree: StateFlow<Tree?> = repo.observeTrees()
        .map { trees -> trees.firstOrNull { it.id == treeId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun changeStatus(newStatus: TreeStatus, spokeWith: String, agreementNotes: String) {
        viewModelScope.launch {
            repo.appendPermissionEvent(
                treeId = treeId,
                event = PermissionEvent(
                    timestamp = Timestamp.now(),
                    status = newStatus,
                    spokeWith = spokeWith.trim(),
                    agreementNotes = agreementNotes.trim(),
                ),
            )
        }
    }

    fun logHarvest(yieldEstimate: String, notes: String) {
        viewModelScope.launch {
            repo.appendHarvestEntry(
                treeId = treeId,
                entry = HarvestEntry(
                    timestamp = Timestamp.now(),
                    yieldEstimate = yieldEstimate,
                    notes = notes.trim(),
                ),
            )
        }
    }

    fun delete(onComplete: () -> Unit) {
        viewModelScope.launch {
            repo.softDelete(treeId)
            onComplete()
        }
    }

    fun openDirections(context: Context, tree: Tree) {
        val lat = tree.location.latitude
        val lng = tree.location.longitude
        val uri = Uri.parse("google.navigation:q=$lat,$lng")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        // Fall back to a generic geo: intent if Google Maps isn't installed.
        runCatching { context.startActivity(intent) }.onFailure {
            val fallback = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:$lat,$lng?q=$lat,$lng"),
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            runCatching { context.startActivity(fallback) }
        }
    }

    companion object {
        const val TREE_ID_KEY = "treeId"

        fun factory(app: SpitePineApp, treeId: String) = viewModelFactory {
            initializer { TreeDetailViewModel(app.treeRepository, treeId) }
        }

        @Suppress("unused")
        fun factoryFromExtras(app: SpitePineApp): (CreationExtras) -> TreeDetailViewModel = {
            val treeId = it[androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY]
                ?.javaClass?.simpleName.orEmpty()
            TreeDetailViewModel(app.treeRepository, treeId)
        }
    }
}
