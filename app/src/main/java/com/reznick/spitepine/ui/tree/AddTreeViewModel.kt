package com.reznick.spitepine.ui.tree

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.firestore.GeoPoint
import com.reznick.spitepine.SpitePineApp
import com.reznick.spitepine.data.model.Tree
import com.reznick.spitepine.data.model.TreeStatus
import com.reznick.spitepine.data.repository.TreeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddTreeUiState(
    val address: String = "",
    val notes: String = "",
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class AddTreeViewModel(private val repo: TreeRepository) : ViewModel() {
    private val _state = MutableStateFlow(AddTreeUiState())
    val state: StateFlow<AddTreeUiState> = _state.asStateFlow()

    fun onAddressChange(value: String) = _state.update { it.copy(address = value) }
    fun onNotesChange(value: String) = _state.update { it.copy(notes = value) }

    fun save() {
        if (_state.value.saving) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            // Hardcoded location until GPS capture lands in chunk B.
            val tree = Tree(
                location = GeoPoint(0.0, 0.0),
                address = _state.value.address.trim(),
                notes = _state.value.notes.trim(),
                status = TreeStatus.SPOTTED,
            )
            val result = repo.addTree(tree)
            _state.update {
                it.copy(
                    saving = false,
                    saved = result.isSuccess,
                    error = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    companion object {
        fun factory(app: SpitePineApp) = viewModelFactory {
            initializer { AddTreeViewModel(app.treeRepository) }
        }
    }
}
