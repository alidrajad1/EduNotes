package com.example.edunotes.ui.screen.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edunotes.data.model.Task
import com.example.edunotes.data.remote.SupabaseClient
import com.example.edunotes.data.repositories.TaskRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TaskUiState {
    data object Loading : TaskUiState()
    data class Success(val tasks: List<Task>) : TaskUiState()
    data class Error(val message: String) : TaskUiState()
}

class TaskViewModel : ViewModel() {
    private val repository = TaskRepository()

    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uploadState = MutableStateFlow(false)
    val uploadState = _uploadState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = TaskUiState.Loading
            try {
                val list = repository.getTasks()
                _uiState.value = TaskUiState.Success(list)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = TaskUiState.Error(e.message ?: "Gagal load tugas")
            }
        }
    }

    fun addTask(title: String, deadline: String, imageBytes: ByteArray?) {
        viewModelScope.launch {
            _uploadState.value = true
            try {
                // 1. Ambil User ID (PENTING!)
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
                if (userId.isEmpty()) throw Exception("Login required")

                val newTask = Task(
                    userId = userId,
                    title = title,
                    deadline = deadline,
                    isCompleted = false
                )
                repository.addTask(newTask, imageBytes)
                loadTasks() // Refresh
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uploadState.value = false
            }
        }
    }

    fun toggleStatus(task: Task) {
        viewModelScope.launch {
            try {
                val newStatus = !task.isCompleted
                // Kirim ke server
                task.id?.let { repository.toggleTaskStatus(it, newStatus) }

                // Refresh UI (Idealnya update lokal dulu biar cepat, tapi reload juga oke)
                loadTasks()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskId)
                loadTasks()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}