package com.example.edunotes.ui.screen.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.edunotes.data.repositories.AuthRepository
import kotlinx.coroutines.runBlocking

@Composable
fun ProfileScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Ini Halaman PROFIL")
            Button(onClick = {
                // Logout sederhana
                runBlocking { AuthRepository().logout() }
                onLogout()
            }) {
                Text("Logout (Tes)")
            }

        }
    }
}