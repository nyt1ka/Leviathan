package com.agrofront.agrohelper.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agrofront.agrohelper.data.AppSettingsRepository
import com.agrofront.agrohelper.data.FieldRepository
import com.agrofront.agrohelper.data.WorkJournalRepository
import com.agrofront.agrohelper.ui.calculators.CalculatorsScreen
import com.agrofront.agrohelper.ui.cadastral.CadastralMapScreen
import com.agrofront.agrohelper.ui.equipment.EquipmentScreen
import com.agrofront.agrohelper.ui.fields.FieldsScreen
import com.agrofront.agrohelper.ui.home.HomeScreen
import com.agrofront.agrohelper.ui.journal.JournalScreen
import com.agrofront.agrohelper.ui.references.ReferencesScreen
import com.agrofront.agrohelper.ui.settings.SettingsScreen
import com.agrofront.agrohelper.ui.work.NewWorkScreen

@Composable
fun AgroHelperApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val journalRepo = remember { WorkJournalRepository(context.applicationContext) }
    val fieldRepo = remember { FieldRepository(context.applicationContext) }
    val settingsRepo = remember { AppSettingsRepository(context.applicationContext) }

    var journalRecords by remember { mutableStateOf(journalRepo.getAll()) }
    var fields by remember { mutableStateOf(fieldRepo.getAll()) }
    var settings by remember { mutableStateOf(settingsRepo.get()) }
    var lastDroneId by remember { mutableStateOf(settingsRepo.getLastDroneId()) }

    fun refreshJournal() { journalRecords = journalRepo.getAll() }
    fun refreshFields() { fields = fieldRepo.getAll() }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onFlightJournal = { navController.navigate("flight_journal") },
                onCalculators = { navController.navigate("calculators") },
                onJournal = { refreshJournal(); navController.navigate("journal") },
                onFields = { refreshFields(); navController.navigate("fields") },
                onReferences = { navController.navigate("references") },
                onCadastralMap = { navController.navigate("cadastral") },
                onSettings = { navController.navigate("settings") }
            )
        }

        composable("flight_journal") {
            NewWorkScreen(
                fields = fields,
                settings = settings,
                lastDroneId = lastDroneId,
                onBack = { navController.popBackStack() },
                onSave = { record, droneId ->
                    journalRepo.add(record)
                    if (settings.rememberLastDrone) {
                        settingsRepo.setLastDroneId(droneId)
                        lastDroneId = droneId
                    }
                    refreshJournal()
                }
            )
        }

        composable("journal") {
            JournalScreen(
                records = journalRecords,
                onBack = { navController.popBackStack() },
                onDelete = { id -> journalRepo.delete(id); refreshJournal() }
            )
        }

        composable("fields") {
            FieldsScreen(
                fields = fields,
                onBack = { navController.popBackStack() },
                onAdd = { fieldRepo.add(it); refreshFields() },
                onDelete = { fieldRepo.delete(it); refreshFields() }
            )
        }

        composable("references") {
            ReferencesScreen(
                onBack = { navController.popBackStack() },
                onEquipment = { navController.navigate("equipment") }
            )
        }

        composable("equipment") {
            EquipmentScreen(onBack = { navController.popBackStack() })
        }

        composable("cadastral") {
            CadastralMapScreen(onBack = { navController.popBackStack() })
        }

        composable("settings") {
            SettingsScreen(
                initial = settings,
                onBack = { navController.popBackStack() },
                onSave = { settingsRepo.save(it); settings = it }
            )
        }

        composable(
            route = "calculators?tank={tank}",
            arguments = listOf(navArgument("tank") { type = NavType.StringType; defaultValue = "" })
        ) { entry ->
            val tank = entry.arguments?.getString("tank")?.toDoubleOrNull()
            CalculatorsScreen(onBack = { navController.popBackStack() }, initialDroneTankLiters = tank)
        }
    }
}
