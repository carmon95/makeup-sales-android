package com.carlos.makeupsales.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carlos.makeupsales.utils.FirebaseAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val uid: String) : AuthUiState()
    data class Error(val message: String?) : AuthUiState()
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // 👇 NUEVO: cuando se crea el ViewModel, revisamos si ya hay usuario logueado
    init {
        val currentUser = FirebaseAuthManager.currentUser()
        if (currentUser != null) {
            _uiState.value = AuthUiState.Success(currentUser.uid)
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = FirebaseAuthManager.auth
                    .createUserWithEmailAndPassword(email, password)
                    .await()
                val uid = result.user?.uid ?: ""
                _uiState.value = AuthUiState.Success(uid)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = FirebaseAuthManager.auth
                    .signInWithEmailAndPassword(email, password)
                    .await()
                val uid = result.user?.uid ?: ""
                _uiState.value = AuthUiState.Success(uid)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message)
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                FirebaseAuthManager.auth.sendPasswordResetEmail(email).await()
                _uiState.value = AuthUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message)
            }
        }
    }

    fun signOut() {
        FirebaseAuthManager.signOut()
        _uiState.value = AuthUiState.Idle
    }
}
