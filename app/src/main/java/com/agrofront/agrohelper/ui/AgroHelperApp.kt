package com.agrofront.agrohelper.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agrofront.agrohelper.ui.calculators.CalculatorsScreen
import com.agrofront.agrohelper.ui.home.HomeScreen
import com.agrofront.agrohelper.ui.placeholder.PlaceholderScreen

@Composable
fun AgroHelperApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onCalculators = { navController.navigate("calculators") },
                onOpenSection = { title -> navController.navigate("placeholder/$title") }
            )
        }
        composable("calculators") {
            CalculatorsScreen(onBack = { navController.popBackStack() })
        }
        composable("placeholder/{title}") { backStackEntry ->
            PlaceholderScreen(
                title = backStackEntry.arguments?.getString("title").orEmpty(),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
