package com.example.edunotes.data.repositories

import com.example.edunotes.data.model.Material
import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
// --- PENTING: Import untuk Update Data (JSON) ---
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
// ------------------------------------------------

class MaterialRepository {
    private val client = SupabaseClient.client

    // 1. GET (Ambil Data)
    suspend fun getMaterials(categoryId: Long? = null): List<Material> {
        return client.from("materials").select {
            if (categoryId != null) {
                filter { eq("category_id", categoryId) }
            }
            order("created_at", Order.ASCENDING) // Urutkan dari yang terbaru
        }.decodeList()
    }

    // 2. ADD (Tambah Data)
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

    // 3. DELETE (Hapus Data) - BARU
    suspend fun deleteMaterial(materialId: Long) {
        client.from("materials").delete {
            filter { eq("id", materialId) }
        }
    }

    // 4. UPDATE (Edit Data) - BARU
    suspend fun updateMaterial(id: Long, title: String, content: String, imageBytes: ByteArray?) {
        var finalUrl: String? = null

        // Jika user upload gambar baru, upload dulu ke storage
        if (imageBytes != null) {
            val fileName = "mat-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("material-images")
            bucket.upload(fileName, imageBytes)
            finalUrl = bucket.publicUrl(fileName)
        }

        // Siapkan data update menggunakan buildJsonObject (Agar aman dari error Serialization)
        val updateData = buildJsonObject {
            put("title", title)
            put("content", content)

            // Hanya update URL gambar jika ada gambar baru
            if (finalUrl != null) {
                put("image_url", finalUrl)
            }
        }

        // Kirim update ke Supabase
        client.from("materials").update(updateData) {
            filter { eq("id", id) }
        }
    }
}