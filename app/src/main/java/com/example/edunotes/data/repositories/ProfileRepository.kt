package com.example.edunotes.data.repositories

import com.example.edunotes.data.model.Profile
import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class ProfileRepository {
    private val db = SupabaseClient.client

    suspend fun getUserProfile(userId: String): Profile? {
        return db.from("profiles").select {
            filter { eq("id", userId) }
        }.decodeSingleOrNull<Profile>()
    }

    suspend fun updateProfile(userId: String, name: String, school: String, imageBytes: ByteArray?) {
        var avatarUrl: String? = null
        // Upload Avatar
        if (imageBytes != null) {
            val fileName = "$userId-${System.currentTimeMillis()}.jpg"
            val bucket = db.storage.from("avatars")
            bucket.upload(fileName, imageBytes){
                upsert = true
            }
            avatarUrl = bucket.publicUrl(fileName)
        }
        // Update DB
        val updateData = mutableMapOf<String, Any>(
            "full_name" to name,
            "school_name" to school
        )
        if (avatarUrl != null) {
            updateData["avatar_url"] = avatarUrl
        }
        db.from("profiles").update(updateData) {
            filter { eq("id", userId) }
        }
    }
}