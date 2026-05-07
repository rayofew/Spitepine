package com.reznick.spitepine.ui.tree

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.reznick.spitepine.SpitePineApp
import com.reznick.spitepine.data.repository.TreeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditTreeUiState(
    val address: String = "",
    val notes: String = "",
    val loaded: Boolean = false,
    val saving: Boolean = false,
    val saveError: String? = null,
    val saved: Boolean = false,
)

class EditTreeViewModel(
    private val repo: TreeRepository,
    private val treeId: String,
) : ViewModel() {
    private val _state = MutableStateFlow(EditTreeUiState())
    val state: StateFlow<EditTreeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val trees = repo.observeTrees().first()
            val tree = trees.firstOrNull { it.id == treeId } ?: return@launch
            _state.update {
                it.copy(
                    address = tree.address,
                    notes = tree.notes,
                    loaded = true,
                )
            }
        }
    }

    fun onAddressChange(value: String) = _state.update { it.copy(address = value) }
    fun onNotesChange(value: String) = _state.update { it.copy(notes = value) }

    fun save() {
        if (_state.value.saving) return
        _state.update { it.copy(saving = true, saveError = null) }
        viewModelScope.launch {
            val updates = mapOf(
                "address" to _state.value.address.trim(),
                "notes" to _state.value.notes.trim(),
            )
            val result = repo.updateTree(treeId, updates)
            _state.update {
                it.copy(
                    saving = false,
                    saved = result.isSuccess,
                    saveError = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    companion object {
        fun factory(app: SpitePineApp, treeId: String) = viewModelFactory {
            initializer { EditTreeViewModel(app.treeRepository, treeId) }
        }
    }
}
