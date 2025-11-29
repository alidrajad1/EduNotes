package com.example.edunotes.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Long? = null,
    @SerialName("user_id") val userId: String,
    val name: String,
    @SerialName("icon_url") val iconUrl: String? = null
)