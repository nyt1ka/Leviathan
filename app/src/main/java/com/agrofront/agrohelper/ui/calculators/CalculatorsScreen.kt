package com.agrofront.agrohelper.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrofront.agrohelper.domain.CalculatorEngine
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsScreen(onBack: () -> Unit) {
    var area by remember { mutableStateOf("") }
    var solutionRate by remember { mutableStateOf("") }
    var productRate by remember { mutableStateOf("") }
    var productUnit by remember { mutableStateOf("л/га") }
    var productUnitExpanded by remember { mutableStateOf(false) }
    var mixTank by remember { mutableStateOf("") }
    var droneTank by remember { mutableStateOf("") }

    var productivityArea by remember { mutableStateOf("") }
    var productivityMinutes by remember { mutableStateOf("") }
    var batterySamples by remember { mutableStateOf("") }

    val result = CalculatorEngine.calculate(
        area.toDoubleSafe(),
        solutionRate.toDoubleSafe(),
        productRate.toDoubleSafe(),
        mixTank.toDoubleSafe(),
        droneTank.toDoubleSafe()
    )

    val productivity = CalculatorEngine.productivityHaPerHour(
        productivityArea.toDoubleSafe(),
        productivityMinutes.toDoubleSafe()
    )

    val batteryValues = CalculatorEngine.parseBatterySamples(batterySamples)
    val batteryStats = CalculatorEngine.batteryStats(batteryValues)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Калькуляторы") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard("Рабочий раствор и вылеты") {
                    NumberField("Площадь, га", area) { area = it }
                    NumberField("Норма рабочего раствора, л/га", solutionRate) { solutionRate = it }
                    NumberField("Норма препарата на 1 га", productRate) { productRate = it }

                    ExposedDropdownMenuBox(
                        expanded = productUnitExpanded,
                        onExpandedChange = { productUnitExpanded = !productUnitExpanded }
                    ) {
                        OutlinedTextField(
                            value = productUnit,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            label = { Text("Единица нормы препарата") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(productUnitExpanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = productUnitExpanded,
                            onDismissRequest = { productUnitExpanded = false }
                        ) {
                            listOf("л/га", "мл/га", "кг/га", "г/га").forEach { unit ->
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

                    NumberField("Объём ёмкости для замеса, л", mixTank) { mixTank = it }
                    NumberField("Объём бака дрона, л", droneTank) { droneTank = it }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    ResultLine("Раствор всего", "${fmt(result.totalSolutionLiters)} л")
                    ResultLine("Препарат всего", "${fmt(result.totalProductAmount)} ${totalUnit(productUnit)}")
                    ResultLine("Количество замесов", result.totalMixes.toString())
                    ResultLine("Замесы", formatLoads(result.fullMixes, result.lastMixLiters))
                    ResultLine("Количество вылетов", result.totalFlights.toString())
                    ResultLine("Вылеты", formatLoads(result.fullFlights, result.lastFlightLiters))
                }
            }

            item {
                SectionCard("Производительность") {
                    NumberField("Обработано, га", productivityArea) { productivityArea = it }
                    NumberField("Время работы, минут", productivityMinutes) { productivityMinutes = it }
                    ResultLine("Фактическая производительность", "${fmt(productivity)} га/ч")
                }
            }

            item {
                SectionCard("Среднее время работы АКБ") {
                    Text("Введите время полётов в формате мм:сс через пробел, запятую или с новой строки.")
                    OutlinedTextField(
                        value = batterySamples,
                        onValueChange = { batterySamples = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Например: 12:40 13:05 11:55 12:30") },
                        minLines = 3,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    ResultLine("Количество замеров", batteryStats.sampleCount.toString())
                    if (batteryStats.sampleCount == 0) {
                        ResultLine("Среднее время", "—")
                        ResultLine("Минимум", "—")
                        ResultLine("Максимум", "—")
                    } else {
                        ResultLine("Среднее время", CalculatorEngine.formatDuration(batteryStats.averageSeconds))
                        ResultLine("Минимум", CalculatorEngine.formatDuration(batteryStats.minSeconds))
                        ResultLine("Максимум", CalculatorEngine.formatDuration(batteryStats.maxSeconds))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            content()
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

@Composable
private fun ResultLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun String.toDoubleSafe(): Double =
    trim().replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

private fun fmt(value: Double): String =
    String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')

private fun totalUnit(rateUnit: String): String = when (rateUnit) {
    "л/га" -> "л"
    "мл/га" -> "мл"
    "кг/га" -> "кг"
    "г/га" -> "г"
    else -> ""
}

private fun formatLoads(fullCount: Int, remainder: Double): String {
    val partial = remainder > 0.000001
    return when {
        fullCount == 0 && !partial -> "—"
        fullCount == 0 -> "${fmt(remainder)} л"
        !partial -> "$fullCount полн."
        else -> "$fullCount полн. + ${fmt(remainder)} л"
    }
}
