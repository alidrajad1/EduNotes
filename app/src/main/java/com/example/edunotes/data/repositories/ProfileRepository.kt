package com.example.edunotes.data.repositories

import com.example.edunotes.data.model.Profile
import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileRepository {
    private val db = SupabaseClient.client

    suspend fun getUserProfile(userId: String): Profile? {
        return db.from("profiles").select {
            filter { eq("id", userId) }
        }.decodeSingleOrNull<Profile>()
    }

    suspend fun updateProfile(userId: String, name: String, school: String, imageBytes: ByteArray?) {
        var avatarUrl: String? = null

        if (imageBytes != null) {
            val fileName = "$userId-${System.currentTimeMillis()}.jpg"
            val bucket = db.storage.from("avatars")
            bucket.upload(fileName, imageBytes) {
                upsert = true
            }
            avatarUrl = bucket.publicUrl(fileName)
        }

        val updateData = buildJsonObject {
            put("id", userId)
            put("full_name", name)
            put("school_name", school)

            if (avatarUrl != null) {
                put("avatar_url", avatarUrl)
            }
        }

        db.from("profiles").upsert(updateData) {
            onConflict = "id"
        }
    }
}