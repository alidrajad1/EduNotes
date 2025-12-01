package com.example.edunotes.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    startDestination : String = "login"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("home")
                    { popUpTo("login") { inclusive = true } }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onRegisterSuccess = {
                    navController.navigate("home")
                    { popUpTo("register") { inclusive = true } }
                }
            )
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("profile") {
            ProfileScreen(navController = navController,
                onLogout = {
                    navController.navigate("login")
                    { popUpTo("home") { inclusive = true } }
                })
        }
        composable("note") {
            NoteScreen(navController = navController)
        }
        composable("task") {
            TaskScreen(navController = navController)
        }
        composable(
            route = NavRoutes.MaterialList.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Ambil data yang dikirim dari Home
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "Materi"

            // Panggil Screen Materi (Kirim ID dan Nama ke sana)
            MaterialScreen(
                navController = navController,
                categoryId = categoryId,
                categoryName = categoryName
            )
        }
    }

}