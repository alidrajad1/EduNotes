package com.example.edunotes.data.remote

import android.content.Context
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

object SupabaseClient {
    private const val SUPABASE_URL = "https://gfxdtjouukuuxwyxizcs.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdmeGR0am91dWt1dXh3eXhpemNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQzNzI1NTQsImV4cCI6MjA3OTk0ODU1NH0.kNKYQvDrIsHOxE4kHMsfeFEZrVn622uzvWeX2k_2qaY"

    // Gunakan lateinit agar bisa di-init nanti di MainActivity dengan Context
    lateinit var client: io.github.jan.supabase.SupabaseClient

    // Panggil fungsi ini di MainActivity.onCreate()
    fun initialize(context: Context) {
        client = createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth) {
                // INI KUNCINYA: Simpan session di HP menggunakan Class di bawah
                sessionManager = AndroidSessionManager(context)
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }
}

// --- CLASS PEMBANTU UNTUK MENYIMPAN SESI KE HP ---
class AndroidSessionManager(context: Context) : SessionManager {
    private val prefs = context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveSession(session: UserSession) {
        // Simpan data session ke file internal HP
        val sessionStr = json.encodeToString(session)
        prefs.edit().putString("session", sessionStr).apply()
    }

    override suspend fun loadSession(): UserSession? {
        // Baca data session saat aplikasi dibuka
        val sessionStr = prefs.getString("session", null) ?: return null
        return try {
            json.decodeFromString(sessionStr)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteSession() {
        // Hapus data saat logout
        prefs.edit().remove("session").apply()
    }
}