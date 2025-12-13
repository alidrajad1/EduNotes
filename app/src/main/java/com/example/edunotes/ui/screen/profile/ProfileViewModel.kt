package com.example.edunotes.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edunotes.data.model.Profile // Pastikan nama model sesuai (Profile/UserProfile)
import com.example.edunotes.data.remote.SupabaseClient
import com.example.edunotes.data.repositories.ProfileRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val profile: Profile?) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel : ViewModel() {
    private val repository = ProfileRepository()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""

                if (userId.isNotEmpty()) {
                    val data = repository.getUserProfile(userId)
                    _uiState.value = ProfileUiState.Success(data)
                } else {
                    _uiState.value = ProfileUiState.Error("User tidak ditemukan")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Gagal memuat profil")
            }
        }
    }

    fun updateProfile(name: String, school: String, imageBytes: ByteArray?) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
                if (userId.isNotEmpty()) {
                    repository.updateProfile(userId, name, school, imageBytes)
                    loadProfile()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}