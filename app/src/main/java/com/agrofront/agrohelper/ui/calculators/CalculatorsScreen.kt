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
    var mixTank by remember { mutableStateOf("") }
    var droneTank by remember { mutableStateOf("") }

    var productivityArea by remember { mutableStateOf("") }
    var productivityMinutes by remember { mutableStateOf("") }

    var batterySamples by remember { mutableStateOf("") }

    val result = CalculatorEngine.calculate(
        areaHa = area.toDoubleSafe(),
        solutionRateLHa = solutionRate.toDoubleSafe(),
        productRatePerHa = productRate.toDoubleSafe(),
        mixTankLiters = mixTank.toDoubleSafe(),
        droneTankLiters = droneTank.toDoubleSafe()
    )

    val productivity = CalculatorEngine.productivityHaPerHour(
        productivityArea.toDoubleSafe(),
        productivityMinutes.toDoubleSafe()
    )

    val batteryValues = batterySamples
        .split(',', ';', '\n', ' ')
        .mapNotNull { it.trim().replace(',', '.').toDoubleOrNull() }

    val avgBattery = CalculatorEngine.averageBatteryMinutes(batteryValues)

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard("Рабочий раствор и вылеты") {
                    NumberField("Площадь, га", area) { area = it }
                    NumberField("Норма рабочего раствора, л/га", solutionRate) { solutionRate = it }
                    NumberField("Норма препарата на 1 га", productRate) { productRate = it }
                    NumberField("Объём ёмкости для замеса, л", mixTank) { mixTank = it }
                    NumberField("Объём бака дрона, л", droneTank) { droneTank = it }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    ResultLine("Раствор всего", "${fmt(result.totalSolutionLiters)} л")
                    ResultLine("Препарат всего", fmt(result.totalProductAmount))
                    ResultLine("Количество замесов", result.totalMixes.toString())
                    ResultLine(
                        "Замесы",
                        if (result.totalMixes == 0) "—"
                        else "${result.fullMixes} полн. + ${fmt(result.lastMixLiters)} л"
                    )
                    ResultLine("Количество вылетов", result.totalFlights.toString())
                    ResultLine(
                        "Вылеты",
                        if (result.totalFlights == 0) "—"
                        else "${result.fullFlights} полн. + ${fmt(result.lastFlightLiters)} л"
                    )
                }
            }

            item {
                SectionCard("Производительность") {
                    NumberField("Обработано, га", productivityArea) { productivityArea = it }
                    NumberField("Время работы, минут", productivityMinutes) { productivityMinutes = it }
                    ResultLine("Производительность", "${fmt(productivity)} га/ч")
                }
            }

            item {
                SectionCard("Среднее время работы АКБ") {
                    Text(
                        "Введите фактическое время полётов в минутах через пробел, запятую или с новой строки.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = batterySamples,
                        onValueChange = { batterySamples = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Например: 12.5 13 11.8 12.7") },
                        minLines = 3,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    ResultLine("Количество замеров", batteryValues.size.toString())
                    ResultLine("Среднее время", "${fmt(avgBattery)} мин")
                    if (batteryValues.isNotEmpty()) {
                        ResultLine("Минимум", "${fmt(batteryValues.min())} мин")
                        ResultLine("Максимум", "${fmt(batteryValues.max())} мин")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                content()
            }
        )
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
    trim().replace(',', '.').toDoubleOrNull() ?: 0.0

private fun fmt(value: Double): String =
    String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
