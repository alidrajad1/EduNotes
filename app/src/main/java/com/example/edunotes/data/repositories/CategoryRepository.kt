package com.example.edunotes.data.repositories

import com.example.edunotes.data.model.Category
import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class CategoryRepository {
    private val client = SupabaseClient.client

    suspend fun getCategories(): List<Category> {
        return client.from("categories").select().decodeList()
    }

    suspend fun addCategory(category: Category, iconBytes: ByteArray?) {
        var finalUrl = category.iconUrl
        // Upload Icon
        if (iconBytes != null) {
            val fileName = "icon-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("category-icons")
            bucket.upload(fileName, iconBytes)
            finalUrl = bucket.publicUrl(fileName)
        }
        // Insert DB
        val newCategory = category.copy(iconUrl = finalUrl)
        client.from("categories").insert(newCategory)
    }

    suspend fun deleteCategory(categoryId: Long) {
        client.from("categories").delete {
            filter { eq("id", categoryId) }
        }
    }
    suspend fun updateCategory(id: Long, name: String, iconBytes: ByteArray?) {
        var finalUrl: String? = null

        // 1. Jika user upload gambar baru, upload dulu
        if (iconBytes != null) {
            val fileName = "icon-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("category-icons")
            bucket.upload(fileName, iconBytes)
            finalUrl = bucket.publicUrl(fileName)
        }

        // 2. Siapkan data yang mau di-update
        val updateData = mutableMapOf<String, Any>("name" to name)

        // Hanya update URL jika ada gambar baru (biar gambar lama gak hilang)
        if (finalUrl != null) {
            updateData["icon_url"] = finalUrl
        }

        // 3. Kirim ke Database
        client.from("categories").update(updateData) {
            filter { eq("id", id) }
        }
    }
}
