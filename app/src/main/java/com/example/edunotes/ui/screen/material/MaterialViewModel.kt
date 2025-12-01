package com.example.edunotes.ui.screen.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edunotes.data.model.Material
import com.example.edunotes.data.remote.SupabaseClient
import com.example.edunotes.data.repositories.MaterialRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MaterialUiState {
    data object Loading : MaterialUiState()
    data class Success(val materials: List<Material>) : MaterialUiState()
    data class Error(val message: String) : MaterialUiState()
}

class MaterialViewModel : ViewModel() {
    private val repository = MaterialRepository()

    private val _uiState = MutableStateFlow<MaterialUiState>(MaterialUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uploadState = MutableStateFlow(false)
    val uploadState = _uploadState.asStateFlow()

    // Fungsi Load dengan Filter Kategori
    fun loadMaterials(categoryId: Long) {
        viewModelScope.launch {
            _uiState.value = MaterialUiState.Loading
            try {
                val list = repository.getMaterials(categoryId)
                _uiState.value = MaterialUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = MaterialUiState.Error(e.message ?: "Gagal memuat materi")
            }
        }
    }

    fun addMaterial(categoryId: Long, title: String, content: String, imageBytes: ByteArray?) {
        viewModelScope.launch {
            _uploadState.value = true
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
                if (userId.isEmpty()) throw Exception("User belum login")

                val newMaterial = Material(
                    userId = userId,
                    categoryId = categoryId, // Simpan ID Kategori
                    title = title,
                    content = content,
                    imageUrl = null
                )

                repository.addMaterial(newMaterial, imageBytes)
                loadMaterials(categoryId) // Refresh list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uploadState.value = false
            }
        }
    }

    fun deleteMaterial(materialId: Long, categoryId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteMaterial(materialId) // Pastikan fungsi ini ada di Repo
                loadMaterials(categoryId) // Refresh list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateMaterial(materialId: Long, categoryId: Long, title: String, content: String, imageBytes: ByteArray?) {
        viewModelScope.launch {
            _uploadState.value = true
            try {
                repository.updateMaterial(materialId, title, content, imageBytes) // Pastikan fungsi ini ada di Repo
                loadMaterials(categoryId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uploadState.value = false
            }
        }
    }
}