package com.reznick.spitepine.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseUser
import com.reznick.spitepine.SpitePineApp
import com.reznick.spitepine.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepo: AuthRepository) : ViewModel() {
    val user: StateFlow<FirebaseUser?> = authRepo.observeUser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepo.currentUser)

    private val _signingIn = MutableStateFlow(false)
    val signingIn: StateFlow<Boolean> = _signingIn.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun signIn(activity: Activity) {
        if (_signingIn.value) return
        _signingIn.value = true
        _error.value = null
        viewModelScope.launch {
            val result = authRepo.signInWithGoogle(activity)
            _signingIn.value = false
            result.exceptionOrNull()?.let { _error.value = it.message ?: it::class.simpleName }
        }
    }

    fun dismissError() {
        _error.value = null
    }

    fun signOut() {
        viewModelScope.launch { authRepo.signOut() }
    }

    companion object {
        fun factory(app: SpitePineApp) = viewModelFactory {
            initializer { AuthViewModel(app.authRepository) }
        }
    }
}
