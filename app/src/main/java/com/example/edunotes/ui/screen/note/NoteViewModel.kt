package com.example.edunotes.ui.screen.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edunotes.data.model.StudyNote
import com.example.edunotes.data.remote.SupabaseClient
import com.example.edunotes.data.repositories.NoteRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NoteUiState {
    data object Loading : NoteUiState()
    data class Success(val notes: List<StudyNote>) : NoteUiState()
    data class Error(val message: String) : NoteUiState()
}

class NoteViewModel : ViewModel() {
    private val repository = NoteRepository()

    private val _uiState = MutableStateFlow<NoteUiState>(NoteUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uploadState = MutableStateFlow(false)
    val uploadState = _uploadState.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _uiState.value = NoteUiState.Loading
            try {
                val list = repository.getNotes()
                _uiState.value = NoteUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = NoteUiState.Error(e.message ?: "Gagal memuat catatan")
            }
        }
    }

    fun addNote(title: String, body: String, imageBytes: ByteArray?) {
        viewModelScope.launch {
            _uploadState.value = true
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
                if (userId.isEmpty()) throw Exception("Login required")

                val newNote = StudyNote(
                    userId = userId,
                    title = title,
                    noteBody = body,
                    mindmapUrl = null
                )
                repository.addNote(newNote, imageBytes)
                loadNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uploadState.value = false
            }
        }
    }

    fun updateNote(id: Long, title: String, body: String, imageBytes: ByteArray?) {
        viewModelScope.launch {
            _uploadState.value = true
            try {
                repository.updateNote(id, title, body, imageBytes)
                loadNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uploadState.value = false
            }
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteNote(id)
                loadNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}