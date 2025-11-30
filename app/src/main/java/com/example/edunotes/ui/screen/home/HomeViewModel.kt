package com.example.edunotes.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edunotes.data.model.Category
import com.example.edunotes.data.remote.SupabaseClient
import com.example.edunotes.data.repositories.CategoryRepository
import com.example.edunotes.data.repositories.ProfileRepository // Import ProfileRepo
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(
        val categories: List<Category>,
        val userName: String // Tambahan field nama
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    private val categoryRepo = CategoryRepository()
    private val profileRepo = ProfileRepository() // Tambahkan repo profil

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uploadState = MutableStateFlow<Boolean>(false)
    val uploadState = _uploadState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // 1. Ambil Kategori
                val categories = categoryRepo.getCategories()

                // 2. Ambil Nama User (Info Header)
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
                val profile = profileRepo.getUserProfile(userId)
                val name = profile?.fullName ?: "Pelajar" // Default jika nama belum diisi

                _uiState.value = HomeUiState.Success(categories, name)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = HomeUiState.Error(e.message ?: "Gagal memuat data")
            }
        }
    }

    // Fungsi Tambah (Add)
    fun addCategory(name: String, iconBytes: ByteArray?) {
        viewModelScope.launch {
            _uploadState.value = true
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
                val newCat = Category(userId = userId, name = name, iconUrl = null)
                categoryRepo.addCategory(newCat, iconBytes)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uploadState.value = false
            }
        }
    }

    // Fungsi Edit (Update) - BARU
    fun updateCategory(id: Long, name: String, iconBytes: ByteArray?) {
        viewModelScope.launch {
            _uploadState.value = true
            try {
                categoryRepo.updateCategory(id, name, iconBytes)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uploadState.value = false
            }
        }
    }

    // Fungsi Hapus
    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                categoryRepo.deleteCategory(categoryId)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}