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

        // 1. Upload Avatar
        if (imageBytes != null) {
            val fileName = "$userId-${System.currentTimeMillis()}.jpg"
            val bucket = db.storage.from("avatars")
            bucket.upload(fileName, imageBytes) {
                upsert = true
            }
            avatarUrl = bucket.publicUrl(fileName)
        }

        // 2. Siapkan Data (Pakai buildJsonObject)
        val updateData = buildJsonObject {
            put("id", userId) // <--- WAJIB DITAMBAHKAN untuk Upsert
            put("full_name", name)
            put("school_name", school)

            if (avatarUrl != null) {
                put("avatar_url", avatarUrl)
            }
        }

        // 3. GANTI 'update' MENJADI 'upsert'
        // Upsert = Kalau data belum ada, buat baru. Kalau sudah ada, update.
        db.from("profiles").upsert(updateData) {
            onConflict = "id" // Kunci uniknya adalah ID
        }
    }
}