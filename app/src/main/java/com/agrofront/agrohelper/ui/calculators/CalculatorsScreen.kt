package com.agrofront.agrohelper.ui.calculators

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.agrofront.agrohelper.domain.CalculatorEngine
import com.agrofront.agrohelper.ui.components.AgroResultRow
import com.agrofront.agrohelper.ui.components.AgroSectionCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsScreen(
    onBack: () -> Unit,
    initialDroneTankLiters: Double? = null
) {
    var area by remember { mutableStateOf("") }
    var solutionRate by remember { mutableStateOf("") }
    var productRate by remember { mutableStateOf("") }
    var productUnit by remember { mutableStateOf("л/га") }
    var productUnitExpanded by remember { mutableStateOf(false) }
    var mixTank by remember { mutableStateOf("") }
    var droneTank by remember(initialDroneTankLiters) {
        mutableStateOf(initialDroneTankLiters?.let(::fmt) ?: "")
    }
    var productivityArea by remember { mutableStateOf("") }
    var productivityTime by remember { mutableStateOf("") }
    var averageBatteryTime by remember { mutableStateOf("") }

    val result = CalculatorEngine.calculate(
        area.toDoubleSafe(),
        solutionRate.toDoubleSafe(),
        productRate.toDoubleSafe(),
        mixTank.toDoubleSafe(),
        droneTank.toDoubleSafe()
    )

    val productivity = CalculatorEngine.productivityHaPerHour(
        productivityArea.toDoubleSafe(),
        CalculatorEngine.parseWorkDurationMinutes(productivityTime)
    )

    val averageBatterySeconds = CalculatorEngine.parseBatteryDurationSeconds(averageBatteryTime)
    val estimatedWorkSeconds = CalculatorEngine.estimateTotalWorkSeconds(
        totalSolutionLiters = result.totalSolutionLiters,
        droneTankLiters = droneTank.toDoubleSafe(),
        averageBatterySeconds = averageBatterySeconds
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Калькуляторы") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Назад") }
                }
            )
        }
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
                    text = "Полевые расчёты",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Заполняй только нужные поля — результаты пересчитываются сразу.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                AgroSectionCard(
                    title = "Рабочий раствор",
                    subtitle = "Общий объём и количество препарата",
                    icon = Icons.Default.WaterDrop
                ) {
                    NumberField("Площадь, га", area) { area = it }
                    NumberField("Норма раствора, л/га", solutionRate) { solutionRate = it }
                    NumberField("Норма препарата на 1 га", productRate) { productRate = it }

                    ExposedDropdownMenuBox(
                        expanded = productUnitExpanded,
                        onExpandedChange = { productUnitExpanded = !productUnitExpanded }
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
                                ExposedDropdownMenuDefaults.TrailingIcon(productUnitExpanded)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = productUnitExpanded,
                            onDismissRequest = { productUnitExpanded = false }
                        ) {
                            productUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        productUnit = unit
                                        productUnitExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    AgroResultRow(
                        "Раствор всего",
                        if (result.totalSolutionLiters > 0) "${fmt(result.totalSolutionLiters)} л" else "—"
                    )
                    AgroResultRow(
                        "Препарат всего",
                        formatProductAmount(result.totalProductAmount, productUnit)
                    )
                }
            }

            item {
                AgroSectionCard(
                    title = "Замес",
                    subtitle = "Сколько замесов и препарата потребуется",
                    icon = Icons.Default.LocalDrink
                ) {
                    NumberField("Ёмкость для замеса, л", mixTank) { mixTank = it }

                    AgroResultRow(
                        "Количество замесов",
                        if (result.totalMixes > 0) result.totalMixes.toString() else "—"
                    )
                    AgroResultRow(
                        "Схема",
                        formatLoads(result.fullMixes, result.lastMixLiters)
                    )

                    if (result.fullMixes > 0) {
                        AgroResultRow(
                            "Препарат на полный замес",
                            formatProductAmount(result.productPerFullMix, productUnit)
                        )
                    }
                    if (result.lastMixLiters > 0) {
                        AgroResultRow("Последний замес", "${fmt(result.lastMixLiters)} л")
                        AgroResultRow(
                            "Препарат в последний замес",
                            formatProductAmount(result.productForLastMix, productUnit)
                        )
                    }
                }
            }

            item {
                AgroSectionCard(
                    title = "Вылеты",
                    subtitle = "Количество баков и последний неполный бак",
                    icon = Icons.Default.LocalDrink
                ) {
                    NumberField("Объём бака дрона, л", droneTank) { droneTank = it }

                    if (initialDroneTankLiters != null) {
                        Text(
                            text = "Бак подставлен из раздела «Техника».",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    AgroResultRow(
                        "Количество вылетов",
                        if (result.totalFlights > 0) result.totalFlights.toString() else "—"
                    )
                    AgroResultRow(
                        "Схема",
                        formatLoads(result.fullFlights, result.lastFlightLiters)
                    )

                    if (result.fullFlights > 0) {
                        AgroResultRow(
                            "Препарат на полный бак",
                            formatProductAmount(result.productPerFullFlight, productUnit)
                        )
                    }
                    if (result.lastFlightLiters > 0) {
                        AgroResultRow("Последний бак", "${fmt(result.lastFlightLiters)} л")
                        AgroResultRow(
                            "Препарат в последний бак",
                            formatProductAmount(result.productForLastFlight, productUnit)
                        )
                    }
                }
            }

            item {
                AgroSectionCard(
                    title = "Производительность",
                    subtitle = "Фактические гектары в час",
                    icon = Icons.Default.Speed
                ) {
                    NumberField("Обработано, га", productivityArea) { productivityArea = it }
                    OutlinedTextField(
                        value = productivityTime,
                        onValueChange = { productivityTime = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Время работы") },
                        supportingText = { Text("Например: 90 или 1:30") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    AgroResultRow(
                        "Производительность",
                        if (productivity > 0) "${fmt(productivity)} га/ч" else "—"
                    )
                }
            }

            item {
                AgroSectionCard(
                    title = "Среднее время работы АКБ",
                    subtitle = "Используется только для оценки общей длительности работы",
                    icon = Icons.Default.BatteryChargingFull
                ) {
                    OutlinedTextField(
                        value = averageBatteryTime,
                        onValueChange = { averageBatteryTime = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Среднее время одной АКБ, мм:сс") },
                        supportingText = { Text("Например: 12:30") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    AgroResultRow(
                        "Примерное общее время работы",
                        if (estimatedWorkSeconds > 0) CalculatorEngine.formatLongDuration(estimatedWorkSeconds) else "—"
                    )
                    Text(
                        text = "Оценка рассчитывается из общего объёма рабочего раствора, площади, нормы вылива, объёма бака дрона и введённого среднего времени АКБ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { Column(modifier = Modifier.padding(bottom = 18.dp)) {} }
        }
    }
}

private val productUnits = listOf("л/га", "мл/га", "кг/га", "г/га")

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

private fun baseProductUnit(rateUnit: String): String = when (rateUnit) {
    "л/га" -> "л"
    "мл/га" -> "мл"
    "кг/га" -> "кг"
    "г/га" -> "г"
    else -> ""
}

private fun formatProductAmount(amount: Double, rateUnit: String): String {
    if (amount <= 0.0) return "—"
    val unit = baseProductUnit(rateUnit)
    val main = "${fmt(amount)} $unit"
    return when {
        rateUnit == "мл/га" && amount >= 1000.0 -> "$main (${fmt(amount / 1000.0)} л)"
        rateUnit == "г/га" && amount >= 1000.0 -> "$main (${fmt(amount / 1000.0)} кг)"
        else -> main
    }
}

private fun formatLoads(fullCount: Int, remainder: Double): String {
    val partial = remainder > 0.000001
    return when {
        fullCount == 0 && !partial -> "—"
        fullCount == 0 -> "${fmt(remainder)} л"
        !partial -> "$fullCount × полный объём"
        else -> "$fullCount × полный объём + ${fmt(remainder)} л"
    }
}
