package com.example.edunotes.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Long? = null,
    @SerialName("user_id") val userId: String,
    val title: String,
    val deadline: String? = null, // Format: YYYY-MM-DD
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("attachment_url") val attachmentUrl: String? = null
)