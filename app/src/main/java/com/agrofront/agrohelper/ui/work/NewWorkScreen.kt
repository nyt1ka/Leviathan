package com.agrofront.agrohelper.ui.work

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.agrofront.agrohelper.data.AppSettings
import com.agrofront.agrohelper.domain.CalculatorEngine
import com.agrofront.agrohelper.domain.DroneCatalog
import com.agrofront.agrohelper.domain.DroneModel
import com.agrofront.agrohelper.domain.FieldRecord
import com.agrofront.agrohelper.domain.WorkRecord
import com.agrofront.agrohelper.ui.components.AgroResultRow
import com.agrofront.agrohelper.ui.components.AgroSectionCard
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewWorkScreen(
    fields: List<FieldRecord>,
    settings: AppSettings,
    lastDroneId: String?,
    onBack: () -> Unit,
    onSave: (WorkRecord, String) -> Unit
) {
    val initialDrone = remember(lastDroneId, settings.rememberLastDrone) {
        if (settings.rememberLastDrone) {
            DroneCatalog.models.firstOrNull { it.id == lastDroneId }
        } else null
    }

    var fieldName by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var fieldExpanded by remember { mutableStateOf(false) }

    var selectedDrone by remember { mutableStateOf<DroneModel?>(initialDrone) }
    var droneExpanded by remember { mutableStateOf(false) }

    var solutionRate by remember { mutableStateOf(fmt(settings.defaultSolutionRateLHa)) }
    var productName by remember { mutableStateOf("") }
    var productRate by remember { mutableStateOf("") }
    var productUnit by remember { mutableStateOf("л/га") }
    var unitExpanded by remember { mutableStateOf(false) }
    var mixTank by remember { mutableStateOf(fmt(settings.defaultMixTankLiters)) }

    val snackbar = remember { SnackbarHostState() }
    var showSavedMessage by remember { mutableStateOf(false) }

    val droneTank = selectedDrone?.sprayTankLiters ?: 0.0

    val result = CalculatorEngine.calculate(
        areaHa = area.toDoubleSafe(),
        solutionRateLHa = solutionRate.toDoubleSafe(),
        productRatePerHa = productRate.toDoubleSafe(),
        mixTankLiters = mixTank.toDoubleSafe(),
        droneTankLiters = droneTank
    )

    val canSave = fieldName.isNotBlank() &&
        selectedDrone != null &&
        area.toDoubleSafe() > 0.0 &&
        solutionRate.toDoubleSafe() > 0.0 &&
        productRate.toDoubleSafe() > 0.0 &&
        mixTank.toDoubleSafe() > 0.0

    LaunchedEffect(showSavedMessage) {
        if (showSavedMessage) {
            snackbar.showSnackbar("Работа сохранена в журнал")
            showSavedMessage = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Журнал полётов") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "Подготовка обработки",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    "Заполни параметры один раз — приложение рассчитает весь план работы.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                AgroSectionCard(
                    title = "1. Поле и техника",
                    subtitle = "Что обрабатываем и каким дроном",
                    icon = Icons.Default.Agriculture
                ) {
                    if (fields.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = fieldExpanded,
                            onExpandedChange = { fieldExpanded = !fieldExpanded }
                        ) {
                            OutlinedTextField(
                                value = fieldName,
                                onValueChange = { fieldName = it },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                label = { Text("Поле / участок") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(fieldExpanded)
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = fieldExpanded,
                                onDismissRequest = { fieldExpanded = false }
                            ) {
                                fields.forEach { field ->
                                    DropdownMenuItem(
                                        text = { Text("${field.name} — ${fmt(field.areaHa)} га") },
                                        onClick = {
                                            fieldName = field.name
                                            area = fmt(field.areaHa)
                                            fieldExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = fieldName,
                            onValueChange = { fieldName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Название поля / участка") },
                            singleLine = true
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = droneExpanded,
                        onExpandedChange = { droneExpanded = !droneExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDrone?.let { "${it.manufacturer} ${it.name}" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text("Дрон") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(droneExpanded)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = droneExpanded,
                            onDismissRequest = { droneExpanded = false }
                        ) {
                            DroneCatalog.models.forEach { drone ->
                                DropdownMenuItem(
                                    text = { Text("${drone.manufacturer} ${drone.name} — ${fmt(drone.sprayTankLiters)} л") },
                                    onClick = {
                                        selectedDrone = drone
                                        droneExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    selectedDrone?.let {
                        AgroResultRow("Бак дрона", "${fmt(it.sprayTankLiters)} л")
                        AgroResultRow("Ширина распыла", it.sprayWidthMeters)
                    }
                }
            }

            item {
                AgroSectionCard(
                    title = "2. Параметры обработки",
                    subtitle = "Площадь, раствор и препарат",
                    icon = Icons.Default.Inventory2
                ) {
                    NumberField("Площадь, га", area) { area = it }
                    NumberField("Норма раствора, л/га", solutionRate) { solutionRate = it }

                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Название препарата") },
                        singleLine = true
                    )

                    NumberField("Норма препарата на 1 га", productRate) { productRate = it }

                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded }
                    ) {
                        OutlinedTextField(
                            value = productUnit,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text("Единица нормы препарата") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            listOf("л/га", "мл/га", "кг/га", "г/га").forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        productUnit = unit
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    NumberField("Ёмкость для замеса, л", mixTank) { mixTank = it }
                }
            }

            item {
                AgroSectionCard(
                    title = "3. Готовый план",
                    subtitle = "Итоговые объёмы, замесы и вылеты",
                    icon = Icons.Default.Calculate
                ) {
                    AgroResultRow("Раствор всего", if (result.totalSolutionLiters > 0) "${fmt(result.totalSolutionLiters)} л" else "—")
                    AgroResultRow("Препарат всего", formatProduct(result.totalProductAmount, productUnit))
                    AgroResultRow("Замесов", if (result.totalMixes > 0) result.totalMixes.toString() else "—")
                    AgroResultRow("Вылетов", if (result.totalFlights > 0) result.totalFlights.toString() else "—")

                    if (result.fullMixes > 0) {
                        AgroResultRow("Препарат на полный замес", formatProduct(result.productPerFullMix, productUnit))
                    }
                    if (result.lastMixLiters > 0) {
                        AgroResultRow("Последний замес", "${fmt(result.lastMixLiters)} л")
                        AgroResultRow("Препарат в последний замес", formatProduct(result.productForLastMix, productUnit))
                    }
                    if (result.lastFlightLiters > 0) {
                        AgroResultRow("Последний бак", "${fmt(result.lastFlightLiters)} л")
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val drone = selectedDrone ?: return@Button
                        val record = WorkRecord(
                            id = System.currentTimeMillis(),
                            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                            fieldName = fieldName.trim(),
                            droneId = drone.id,
                            droneName = "${drone.manufacturer} ${drone.name}",
                            areaHa = area.toDoubleSafe(),
                            solutionRateLHa = solutionRate.toDoubleSafe(),
                            productName = productName.trim(),
                            productRatePerHa = productRate.toDoubleSafe(),
                            productUnit = productUnit,
                            mixTankLiters = mixTank.toDoubleSafe(),
                            droneTankLiters = drone.sprayTankLiters,
                            totalSolutionLiters = result.totalSolutionLiters,
                            totalProductAmount = result.totalProductAmount,
                            totalMixes = result.totalMixes,
                            totalFlights = result.totalFlights
                        )
                        onSave(record, drone.id)
                        showSavedMessage = true
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    androidx.compose.material3.Icon(Icons.Default.Save, contentDescription = null)
                    Text("Сохранить в журнал", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

private fun String.toDoubleSafe(): Double =
    trim().replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

private fun fmt(value: Double): String =
    String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')

private fun formatProduct(amount: Double, rateUnit: String): String {
    if (amount <= 0.0) return "—"
    val unit = when (rateUnit) {
        "л/га" -> "л"
        "мл/га" -> "мл"
        "кг/га" -> "кг"
        "г/га" -> "г"
        else -> ""
    }
    return "${fmt(amount)} $unit"
}
