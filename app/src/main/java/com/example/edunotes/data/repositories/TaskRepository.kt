package com.example.edunotes.data.repositories

import com.example.edunotes.data.model.Task
import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TaskRepository {
    private val client = SupabaseClient.client

    // Read
    suspend fun getTasks(): List<Task> {
        return client.from("tasks").select {
            order("deadline", Order.ASCENDING)
        }.decodeList()
    }

    // Insert
    suspend fun addTask(task: Task, attachmentBytes: ByteArray?) {
        var finalUrl: String? = null

        // Upload Foto Soal (Jika ada)
        if (attachmentBytes != null) {
            val fileName = "task-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("task-attachments")
            bucket.upload(fileName, attachmentBytes)
            finalUrl = bucket.publicUrl(fileName)
        }

        val newTask = task.copy(attachmentUrl = finalUrl)
        client.from("tasks").insert(newTask)
    }

    suspend fun toggleTaskStatus(taskId: Long, isCompleted: Boolean) {
        val updateData = buildJsonObject {
            put("is_completed", isCompleted)
        }
        client.from("tasks").update(updateData) {
            filter { eq("id", taskId) }
        }
    }

    suspend fun deleteTask(taskId: Long) {
        client.from("tasks").delete {
            filter { eq("id", taskId) }
        }
    }
}