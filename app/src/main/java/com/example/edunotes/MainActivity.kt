package com.example.edunotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.edunotes.data.remote.SupabaseClient
import com.example.edunotes.ui.nav.AppNavHost
import com.example.edunotes.ui.theme.EduNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Wajib: Inisialisasi Supabase
        SupabaseClient.initialize(applicationContext)

        setContent {
            EduNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. Buat NavController
                    val navController = rememberNavController()

                    // 3. Panggil AppNavHost (Logic login sudah diurus di dalamnya)
                    AppNavHost(navController = navController)
                }
            }
        }
    }
}