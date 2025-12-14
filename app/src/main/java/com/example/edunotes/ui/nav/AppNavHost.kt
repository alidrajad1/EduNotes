package com.example.edunotes.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.edunotes.ui.screen.auth.AuthViewModel
import com.example.edunotes.ui.screen.auth.LoginScreen
import com.example.edunotes.ui.screen.auth.RegisterScreen
import com.example.edunotes.ui.screen.home.HomeScreen
import com.example.edunotes.ui.screen.material.MaterialScreen
import com.example.edunotes.ui.screen.note.NoteScreen
import com.example.edunotes.ui.screen.profile.ProfileScreen
import com.example.edunotes.ui.screen.task.TaskScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    // Kita Inject ViewModel di sini agar bisa memantau status global
    authViewModel: AuthViewModel = viewModel()
) {
    // Ambil status login dari ViewModel
    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    // --- LOGIKA NAVIGASI OTOMATIS (REACTIVE) ---
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            // Jika status berubah jadi Login -> Pindah ke Home
            // popUpTo(0) menghapus semua history agar tidak bisa back ke login
            navController.navigate(NavRoutes.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        } else {
            // Jika status berubah jadi Logout -> Pindah ke Login
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    // -------------------------------------------

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route // Start default (akan ditimpa oleh LaunchedEffect)
    ) {
        // --- AUTH ---
        composable(NavRoutes.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(NavRoutes.Register.route) },
                onLoginSuccess = {
                    // KOSONGKAN SAJA: Navigasi sudah ditangani LaunchedEffect di atas
                }
            )
        }
        composable(NavRoutes.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(NavRoutes.Login.route) },
                onRegisterSuccess = {
                    // KOSONGKAN SAJA
                }
            )
        }

        // --- MAIN FEATURES ---
        composable(NavRoutes.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(NavRoutes.Profile.route) {
            ProfileScreen(
                navController = navController,
                onLogout = {
                    // Panggil fungsi logout di ViewModel.
                    // Ini akan mengubah state isLoggedIn -> false -> Trigger LaunchedEffect -> Pindah ke Login
                    authViewModel.logout()
                }
            )
        }
        composable(NavRoutes.Notes.route) {
            NoteScreen(navController = navController)
        }
        composable(NavRoutes.Tasks.route) {
            TaskScreen(navController = navController)
        }

        // --- MATERIAL DETAIL ---
        composable(
            route = NavRoutes.MaterialList.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "Materi"

            MaterialScreen(
                navController = navController,
                categoryId = categoryId,
                categoryName = categoryName
            )
        }
    }
}