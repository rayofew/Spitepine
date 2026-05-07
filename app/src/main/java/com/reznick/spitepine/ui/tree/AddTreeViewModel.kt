package com.reznick.spitepine.ui.tree

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.firestore.GeoPoint
import com.reznick.spitepine.SpitePineApp
import com.reznick.spitepine.data.model.Tree
import com.reznick.spitepine.data.model.TreeStatus
import com.reznick.spitepine.data.repository.TreeRepository
import com.reznick.spitepine.util.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddTreeUiState(
    val address: String = "",
    val addressManuallyEdited: Boolean = false,
    val notes: String = "",
    val capturedLocation: GeoPoint? = null,
    val capturing: Boolean = false,
    val captureError: String? = null,
    val saving: Boolean = false,
    val saveError: String? = null,
    val saved: Boolean = false,
)

class AddTreeViewModel(private val repo: TreeRepository) : ViewModel() {
    private val _state = MutableStateFlow(AddTreeUiState())
    val state: StateFlow<AddTreeUiState> = _state.asStateFlow()

    fun onAddressChange(value: String) =
        _state.update { it.copy(address = value, addressManuallyEdited = true) }

    fun onNotesChange(value: String) = _state.update { it.copy(notes = value) }

    fun captureLocation(context: Context) {
        if (_state.value.capturing) return
        _state.update { it.copy(capturing = true, captureError = null) }
        viewModelScope.launch {
            val location = runCatching { LocationHelper.getCurrentLocation(context) }
                .getOrElse {
                    _state.update {
                        it.copy(capturing = false, captureError = it.captureError ?: "Couldn't get location")
                    }
                    return@launch
                }
            if (location == null) {
                _state.update {
                    it.copy(
                        capturing = false,
                        captureError = "No location returned. Make sure location is on and try again.",
                    )
                }
                return@launch
            }
            val geo = GeoPoint(location.latitude, location.longitude)
            _state.update { it.copy(capturedLocation = geo) }

            // Reverse-geocode in the background; only fill if the user hasn't typed.
            val address = LocationHelper.reverseGeocode(context, geo.latitude, geo.longitude)
            _state.update { current ->
                val keepAddress = current.addressManuallyEdited
                current.copy(
                    capturing = false,
                    address = if (keepAddress || address.isNullOrBlank()) current.address else address,
                )
            }
        }
    }

    fun save() {
        val current = _state.value
        val location = current.capturedLocation ?: return
        if (current.saving) return
        _state.update { it.copy(saving = true, saveError = null) }
        viewModelScope.launch {
            val tree = Tree(
                location = location,
                address = current.address.trim(),
                notes = current.notes.trim(),
                status = TreeStatus.SPOTTED,
            )
            val result = repo.addTree(tree)
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
        fun factory(app: SpitePineApp) = viewModelFactory {
            initializer { AddTreeViewModel(app.treeRepository) }
        }
    }
}
