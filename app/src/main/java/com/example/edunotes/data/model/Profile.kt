package com.example.edunotes.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Profile(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("school_name") val schoolName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)