package com.example.edunotes.data.repositories

import com.example.edunotes.data.model.StudyNote
import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class NoteRepository {
    private val client = SupabaseClient.client

    // GET
    suspend fun getNotes(): List<StudyNote> {
        return client.from("study_notes").select {
            order("created_at", Order.ASCENDING)
        }.decodeList()
    }

    // ADD
    suspend fun addNote(note: StudyNote, imageBytes: ByteArray?) {
        var finalUrl: String? = null
        if (imageBytes != null) {
            val fileName = "note-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("note-attachments")
            bucket.upload(fileName, imageBytes)
            finalUrl = bucket.publicUrl(fileName)
        }
        val newNote = note.copy(mindmapUrl = finalUrl)
        client.from("study_notes").insert(newNote)
    }

    // DELETE
    suspend fun deleteNote(noteId: Long) {
        client.from("study_notes").delete {
            filter { eq("id", noteId) }
        }
    }

    // UPDATE
    suspend fun updateNote(id: Long, title: String, body: String, imageBytes: ByteArray?) {
        var finalUrl: String? = null

        // Upload gambar baru jika ada
        if (imageBytes != null) {
            val fileName = "note-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("note-attachments")
            bucket.upload(fileName, imageBytes)
            finalUrl = bucket.publicUrl(fileName)
        }

        // Pakai buildJsonObject agar aman
        val updateData = buildJsonObject {
            put("title", title)
            put("note_body", body)
            if (finalUrl != null) {
                put("note-attachments_url", finalUrl)
            }
        }

        client.from("study_notes").update(updateData) {
            filter { eq("id", id) }
        }
    }
}