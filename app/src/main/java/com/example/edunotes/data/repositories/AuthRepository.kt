package com.example.edunotes.data.repositories

import com.example.edunotes.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository {
    private val auth = SupabaseClient.client.auth

    fun getCurrentUser() = auth.currentUserOrNull()

    suspend fun login(email: String, pass: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = pass
        }
    }

    suspend fun register(email: String, pass: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = pass
        }
    }

    suspend fun logout() {
        auth.signOut()
    }
}