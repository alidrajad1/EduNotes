package com.example.edunotes.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edunotes.data.remote.SupabaseClient
import com.example.edunotes.data.repositories.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState = _authState.asStateFlow()

    // --- STATE STATUS LOGIN (REAKTIF) ---
    // Cek awal: Apakah ada sesi tersimpan?
    private val _isUserLoggedIn = MutableStateFlow(
        SupabaseClient.client.auth.currentSessionOrNull() != null
    )
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    init {
        // Pantau perubahan sesi (Login/Logout/Expired) secara otomatis
        viewModelScope.launch {
            SupabaseClient.client.auth.sessionStatus.collect { status ->
                _isUserLoggedIn.value = (status is SessionStatus.Authenticated)
            }
        }
    }
    // -------------------------------------

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                repository.login(email, pass)
                _authState.value = AuthUiState.Success
                // Tidak perlu mengubah _isUserLoggedIn manual,
                // karena listener di init {} akan mendeteksinya otomatis
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Login Gagal")
            }
        }
    }

    fun register(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                repository.register(email, pass)
                _authState.value = AuthUiState.Success
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Register Gagal")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        _authState.value = AuthUiState.Idle
    }
}