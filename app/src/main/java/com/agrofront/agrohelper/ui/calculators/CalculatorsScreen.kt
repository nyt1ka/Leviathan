package com.agrofront.agrohelper.ui.calculators

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
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
    var productivityTime by remember { mutableStateOf("") }

    var batterySamples by remember { mutableStateOf("") }

    val result = CalculatorEngine.calculate(
        areaHa = area.toDoubleSafe(),
        solutionRateLHa = solutionRate.toDoubleSafe(),
        productRatePerHa = productRate.toDoubleSafe(),
        mixTankLiters = mixTank.toDoubleSafe(),
        droneTankLiters = droneTank.toDoubleSafe()
    )

    val productivityMinutes = CalculatorEngine.parseWorkDurationMinutes(productivityTime)
    val productivity = CalculatorEngine.productivityHaPerHour(
        areaHa = productivityArea.toDoubleSafe(),
        minutes = productivityMinutes
    )

    val batteryValues = CalculatorEngine.parseBatterySamples(batterySamples)
    val batteryStats = CalculatorEngine.batteryStats(batteryValues)

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
                SectionCard("Рабочий раствор") {
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
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text("Единица нормы препарата") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = productUnitExpanded
                                )
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

                    ResultLine(
                        "Количество раствора",
                        if (result.totalSolutionLiters > 0.0) {
                            "${fmt(result.totalSolutionLiters)} л"
                        } else {
                            "—"
                        }
                    )
                    ResultLine(
                        "Количество препарата",
                        formatProductAmount(result.totalProductAmount, productUnit)
                    )
                }
            }

            item {
                SectionCard("Замес рабочего раствора") {
                    NumberField("Объём ёмкости для замеса, л", mixTank) { mixTank = it }

                    if (solutionRate.toDoubleSafe() <= 0.0) {
                        HintText("Укажите норму рабочего раствора, чтобы рассчитать препарат на замес.")
                    }

                    ResultLine(
                        "Количество замесов",
                        if (result.totalMixes > 0) result.totalMixes.toString() else "—"
                    )
                    ResultLine(
                        "Схема замесов",
                        formatLoads(result.fullMixes, result.lastMixLiters)
                    )

                    if (result.fullMixes > 0) {
                        ResultLine(
                            "Препарат на полный замес",
                            formatProductAmount(result.productPerFullMix, productUnit)
                        )
                    }

                    if (result.lastMixLiters > 0.0) {
                        ResultLine(
                            "Последний замес",
                            "${fmt(result.lastMixLiters)} л"
                        )
                        ResultLine(
                            "Препарат в последний замес",
                            formatProductAmount(result.productForLastMix, productUnit)
                        )
                    }
                }
            }

            item {
                SectionCard("Вылеты") {
                    NumberField("Объём бака дрона, л", droneTank) { droneTank = it }

                    ResultLine(
                        "Количество вылетов",
                        if (result.totalFlights > 0) result.totalFlights.toString() else "—"
                    )
                    ResultLine(
                        "Схема заправок",
                        formatLoads(result.fullFlights, result.lastFlightLiters)
                    )

                    if (result.fullFlights > 0) {
                        ResultLine(
                            "Препарат на полный бак",
                            formatProductAmount(result.productPerFullFlight, productUnit)
                        )
                    }

                    if (result.lastFlightLiters > 0.0) {
                        ResultLine(
                            "Последний бак",
                            "${fmt(result.lastFlightLiters)} л"
                        )
                        ResultLine(
                            "Препарат в последний бак",
                            formatProductAmount(result.productForLastFlight, productUnit)
                        )
                    }
                }
            }

            item {
                SectionCard("Производительность") {
                    NumberField("Обработано, га", productivityArea) {
                        productivityArea = it
                    }

                    OutlinedTextField(
                        value = productivityTime,
                        onValueChange = { productivityTime = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Время работы: минуты или ч:мин") },
                        supportingText = { Text("Например: 90 или 1:30") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    ResultLine(
                        "Фактическая производительность",
                        if (productivity > 0.0) "${fmt(productivity)} га/ч" else "—"
                    )
                }
            }

            item {
                SectionCard("Среднее время работы АКБ") {
                    Text(
                        "Введите фактическое время полётов в формате мм:сс. " +
                            "Разделяйте значения пробелом, запятой или новой строкой.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = batterySamples,
                        onValueChange = { batterySamples = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Например: 12:40 13:05 11:55 12:30") },
                        minLines = 3,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    ResultLine(
                        "Количество замеров",
                        if (batteryStats.sampleCount > 0) {
                            batteryStats.sampleCount.toString()
                        } else {
                            "—"
                        }
                    )

                    if (batteryStats.sampleCount > 0) {
                        ResultLine(
                            "Среднее время",
                            CalculatorEngine.formatDuration(batteryStats.averageSeconds)
                        )
                        ResultLine(
                            "Минимум",
                            CalculatorEngine.formatDuration(batteryStats.minSeconds)
                        )
                        ResultLine(
                            "Максимум",
                            CalculatorEngine.formatDuration(batteryStats.maxSeconds)
                        )
                    } else {
                        ResultLine("Среднее время", "—")
                        ResultLine("Минимум", "—")
                        ResultLine("Максимум", "—")
                    }
                }
            }

            item {
                TextButton(
                    onClick = {
                        area = ""
                        solutionRate = ""
                        productRate = ""
                        mixTank = ""
                        droneTank = ""
                        productivityArea = ""
                        productivityTime = ""
                        batterySamples = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Очистить все калькуляторы")
                }
            }
        }
    }
}

private val productUnits = listOf("л/га", "мл/га", "кг/га", "г/га")

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
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
        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HintText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall
    )
}

private fun String.toDoubleSafe(): Double =
    trim()
        .replace(',', '.')
        .toDoubleOrNull()
        ?.coerceAtLeast(0.0)
        ?: 0.0

private fun fmt(value: Double): String =
    String
        .format(Locale.US, "%.2f", value)
        .trimEnd('0')
        .trimEnd('.')

private fun baseProductUnit(rateUnit: String): String =
    when (rateUnit) {
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
        rateUnit == "мл/га" && amount >= 1000.0 ->
            "$main (${fmt(amount / 1000.0)} л)"

        rateUnit == "г/га" && amount >= 1000.0 ->
            "$main (${fmt(amount / 1000.0)} кг)"

        else -> main
    }
}

private fun formatLoads(fullCount: Int, remainder: Double): String {
    val hasRemainder = remainder > 0.000001

    return when {
        fullCount == 0 && !hasRemainder -> "—"
        fullCount == 0 -> "${fmt(remainder)} л"
        !hasRemainder -> "$fullCount × полный объём"
        else -> "$fullCount × полный объём + ${fmt(remainder)} л"
    }
}
