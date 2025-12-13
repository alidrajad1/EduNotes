package com.example.edunotes.data.repositories

import com.example.edunotes.data.model.Material
import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MaterialRepository {
    private val client = SupabaseClient.client

    suspend fun getMaterials(categoryId: Long? = null): List<Material> {
        return client.from("materials").select {
            if (categoryId != null) {
                filter { eq("category_id", categoryId) }
            }
            order("created_at", Order.ASCENDING) // Urutkan dari yang terbaru
        }.decodeList()
    }

    suspend fun addMaterial(material: Material, imageBytes: ByteArray?) {
        var finalUrl: String? = null

        if (imageBytes != null) {
            val fileName = "mat-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("material-images")
            bucket.upload(fileName, imageBytes)
            finalUrl = bucket.publicUrl(fileName)
        }

        val newMaterial = material.copy(imageUrl = finalUrl)
        client.from("materials").insert(newMaterial)
    }

    suspend fun deleteMaterial(materialId: Long) {
        client.from("materials").delete {
            filter { eq("id", materialId) }
        }
    }

    suspend fun updateMaterial(id: Long, title: String, content: String, imageBytes: ByteArray?) {
        var finalUrl: String? = null

        if (imageBytes != null) {
            val fileName = "mat-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("material-images")
            bucket.upload(fileName, imageBytes)
            finalUrl = bucket.publicUrl(fileName)
        }

            val updateData = buildJsonObject {
            put("title", title)
            put("content", content)

            if (finalUrl != null) {
                put("image_url", finalUrl)
            }
        }

        client.from("materials").update(updateData) {
            filter { eq("id", id) }
        }
    }
}