package com.example.edunotes.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Material(
    val id: Long? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("category_id") val categoryId: Long? = null,
    val title: String,
    val content: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)