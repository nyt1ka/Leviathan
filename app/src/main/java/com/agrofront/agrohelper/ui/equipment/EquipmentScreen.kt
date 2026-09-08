package com.agrofront.agrohelper.ui.equipment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.agrofront.agrohelper.domain.DroneCatalog
import com.agrofront.agrohelper.domain.DroneModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(
    onBack: () -> Unit,
    onUseInCalculator: (Double) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("Все") }

    val filtered = DroneCatalog.models.filter { model ->
        val manufacturerMatches = manufacturer == "Все" || model.manufacturer == manufacturer
        val queryMatches = query.isBlank() ||
            model.name.contains(query, ignoreCase = true) ||
            model.manufacturer.contains(query, ignoreCase = true)
        manufacturerMatches && queryMatches
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Техника") },
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
                    text = "Каталог агродронов",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Выбери модель, посмотри характеристики и сразу подставь бак в калькулятор.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Поиск") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DroneCatalog.manufacturers.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { item ->
                                FilterChip(
                                    selected = manufacturer == item,
                                    onClick = { manufacturer = item },
                                    label = { Text(item) }
                                )
                            }
                        }
                    }
                }
            }

            items(filtered, key = { it.id }) { model ->
                DroneCard(model, onUseInCalculator)
            }

            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = "Ничего не найдено.",
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            item { Column(modifier = Modifier.padding(bottom = 18.dp)) {} }
        }
    }
}

@Composable
private fun DroneCard(
    model: DroneModel,
    onUseInCalculator: (Double) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DronePhoto(model)

            Text(
                text = model.manufacturer,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = model.name,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoPill("${fmt(model.sprayTankLiters)} л бак")
                model.maxLiquidFlowLMin?.let { InfoPill("${fmt(it)} л/мин") }
            }

            SpecLine("Ширина распыла", model.sprayWidthMeters)
            model.dropletSizeMicrons?.let { SpecLine("Размер капли", it) }

            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Скрыть подробности" else "Показать подробности")
            }

            if (expanded) {
                HorizontalDivider()
                SpecLine("Схема", model.rotorLayout)
                SpecLine("Распылители", model.nozzleSystem)
                SpecLine("Масса", model.aircraftWeightKg)
                SpecLine("Размеры", model.dimensions)
                model.protectionRating?.let { SpecLine("Защита", it) }
                model.maxSpeedMps?.let { SpecLine("Макс. скорость", "${fmt(it)} м/с") }
                model.maxFlightDistance?.let { SpecLine("Дальность", it) }
                model.positioning?.let { SpecLine("Позиционирование", it) }
                model.obstacleAvoidance?.let { SpecLine("Обход препятствий", it) }
                model.terrainFollowing?.let { SpecLine("Рельеф", it) }
                model.motorSystem?.let { SpecLine("Двигатели", it) }
                model.propellerSystem?.let { SpecLine("Пропеллеры", it) }

                if (model.spreadTankLiters != null) {
                    SpecLine("Бункер", "${fmt(model.spreadTankLiters)} л")
                }
                if (model.spreadPayloadKg != null) {
                    SpecLine("Нагрузка бункера", "${fmt(model.spreadPayloadKg)} кг")
                }
                model.spreading?.let { SpecLine("Внесение гранул", it) }

                model.battery?.let { SpecLine("АКБ", it) }
                model.batteryCapacity?.let { SpecLine("Параметры АКБ", it) }
                model.charger?.let { SpecLine("Зарядная система", it) }
                model.chargeTime?.let { SpecLine("Зарядка", it) }
                model.productivity?.let { SpecLine("Производительность", it) }
                model.operatingTemperature?.let { SpecLine("Температура", it) }

                Text(
                    text = model.notes,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Источник: ${model.sourceLabel}. Проверено: ${model.verified}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = { onUseInCalculator(model.sprayTankLiters) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Использовать в калькуляторе")
            }
        }
    }
}

@Composable
private fun InfoPill(text: String) {
    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun DronePhoto(model: DroneModel) {
    SubcomposeAsyncImage(
        model = model.photoUrl,
        contentDescription = "Фото ${model.name}",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
    ) {
        when (painter.state) {
            is coil.compose.AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Фото ${model.name}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.58f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun fmt(value: Double): String =
    String.format(Locale.US, "%.1f", value).trimEnd('0').trimEnd('.')
