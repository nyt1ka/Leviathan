package com.agrofront.agrohelper.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agrofront.agrohelper.ui.calculators.CalculatorsScreen
import com.agrofront.agrohelper.ui.equipment.EquipmentScreen
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
                onEquipment = { navController.navigate("equipment") },
                onPlaceholder = { title ->
                    navController.navigate("placeholder/${title}")
                }
            )
        }

        composable(
            route = "calculators?tank={tank}",
            arguments = listOf(
                navArgument("tank") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entry ->
            val tank = entry.arguments
                ?.getString("tank")
                ?.toDoubleOrNull()

            CalculatorsScreen(
                onBack = { navController.popBackStack() },
                initialDroneTankLiters = tank
            )
        }

        composable("equipment") {
            EquipmentScreen(
                onBack = { navController.popBackStack() },
                onUseInCalculator = { liters ->
                    navController.navigate("calculators?tank=$liters")
                }
            )
        }

        composable(
            route = "placeholder/{title}",
            arguments = listOf(navArgument("title") { type = NavType.StringType })
        ) { entry ->
            PlaceholderScreen(
                title = entry.arguments?.getString("title") ?: "Раздел",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
