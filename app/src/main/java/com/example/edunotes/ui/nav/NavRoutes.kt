package com.example.edunotes.ui.nav

sealed class NavRoutes(val route: String) {
    data object Login : NavRoutes("login")
    data object Register : NavRoutes("register")
    data object Home : NavRoutes("home")
    data object Profile : NavRoutes("profile")
    data object Notes : NavRoutes("note")
    data object Tasks : NavRoutes("task")

    object MaterialList {
        const val route = "material_list/{categoryId}/{categoryName}"

        fun createRoute(categoryId: Long, categoryName: String): String {
            return "material_list/$categoryId/$categoryName"
        }
    }
}