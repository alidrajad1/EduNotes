package com.example.edunotes.data.repositories

import com.example.edunotes.data.model.Task
import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage

class TaskRepository {
    private val client = SupabaseClient.client

    suspend fun getTasks(): List<Task> {
        return client.from("tasks").select {
            order("deadline", Order.ASCENDING)
        }.decodeList()
    }

    suspend fun addTask(task: Task, attachmentBytes: ByteArray?) {
        var finalUrl: String? = null
        // Upload Foto Soal/Lampiran
        if (attachmentBytes != null) {
            val fileName = "task-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("task-attachments")
            bucket.upload(fileName, attachmentBytes)
            finalUrl = bucket.publicUrl(fileName)
        }
        // Insert DB
        val newTask = task.copy(attachmentUrl = finalUrl)
        client.from("tasks").insert(newTask)
    }

    suspend fun toggleTaskStatus(taskId: Long, isCompleted: Boolean) {
        client.from("tasks").update(
            { set("is_completed", isCompleted) }
        ) {
            filter { eq("id", taskId) }
        }
    }
}