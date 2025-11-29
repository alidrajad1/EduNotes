package com.example.edunotes.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
    private const val SUPABASE_URL = "https://gfxdtjouukuuxwyxizcs.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdmeGR0am91dWt1dXh3eXhpemNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQzNzI1NTQsImV4cCI6MjA3OTk0ODU1NH0.kNKYQvDrIsHOxE4kHMsfeFEZrVn622uzvWeX2k_2qaY"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ){
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }

    fun session(): UserSession? = client.auth.currentSessionOrNull()
}
