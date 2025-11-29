package com.example.edunotes.data.repositories

import com.example.edunotes.data.model.Material
import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class MaterialRepository {
    private val client = SupabaseClient.client

    suspend fun getMaterials(categoryId: Long? = null): List<Material> {
        return client.from("materials").select {
            if (categoryId != null) {
                filter { eq("category_id", categoryId) }
            }
        }.decodeList()
    }

    suspend fun addMaterial(material: Material, imageBytes: ByteArray?) {
        var finalUrl: String? = null
        // Upload Gambar Materi
        if (imageBytes != null) {
            val fileName = "mat-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("material-images")
            bucket.upload(fileName, imageBytes)
            finalUrl = bucket.publicUrl(fileName)
        }
        // Insert DB
        val newMaterial = material.copy(imageUrl = finalUrl)
        client.from("materials").insert(newMaterial)
    }
}