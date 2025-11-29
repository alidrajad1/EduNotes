package com.example.edunotes.data.repository

import com.example.edunotes.data.model.StudyNote
import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class NoteRepository {
    private val client = SupabaseClient.client

    suspend fun getNotes(): List<StudyNote> {
        return client.from("study_notes").select().decodeList()
    }

    suspend fun addNote(note: StudyNote, imageBytes: ByteArray?) {
        var finalUrl: String? = null
        // Upload note
        if (imageBytes != null) {
            val fileName = "image-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("note-attachments")
            bucket.upload(fileName, imageBytes)
            finalUrl = bucket.publicUrl(fileName)
        }
        // Insert DB
        val newNote = note.copy(mindmapUrl = finalUrl)
        client.from("study_notes").insert(newNote)
    }
}