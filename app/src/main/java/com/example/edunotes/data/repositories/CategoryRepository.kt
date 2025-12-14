package com.example.edunotes.data.repositories

import com.example.edunotes.data.model.Category
import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CategoryRepository {
    private val client = SupabaseClient.client

    suspend fun getCategories(): List<Category> {
        return client.from("categories").select().decodeList()
    }

    suspend fun addCategory(category: Category, iconBytes: ByteArray?) {
        var finalUrl = category.iconUrl

        if (iconBytes != null) {
            val fileName = "icon-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("category-icons")
            bucket.upload(fileName, iconBytes)
            finalUrl = bucket.publicUrl(fileName)
        }

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

        if (iconBytes != null) {
            val fileName = "icon-${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("category-icons")
            bucket.upload(fileName, iconBytes)
            finalUrl = bucket.publicUrl(fileName)
        }

        val updateData = buildJsonObject {
            put("name", name) // Update nama

            if (finalUrl != null) {
                put("icon_url", finalUrl)
            }
        }

        client.from("categories").update(updateData) {
            filter { eq("id", id) }
        }
    }
}
