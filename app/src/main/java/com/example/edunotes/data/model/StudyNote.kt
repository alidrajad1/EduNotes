package com.example.edunotes.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudyNote(
    val id: Long? = null,
    @SerialName("user_id") val userId: String,
    val title: String,
    @SerialName("note_body") val noteBody: String,
    @SerialName("mindmap_url") val mindmapUrl: String? = null
)