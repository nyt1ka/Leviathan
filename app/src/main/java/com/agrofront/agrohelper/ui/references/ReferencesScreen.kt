package com.agrofront.agrohelper.ui.references

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReferencesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Справочники") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Быстрые подсказки",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                InfoCard(
                    "Единицы нормы препарата",
                    "Жидкие препараты: л/га или мл/га. Сухие: кг/га или г/га. Калькулятор сохраняет ту единицу, в которой введена норма."
                )
            }

            item {
                InfoCard(
                    "Рабочий раствор",
                    "Общий объём раствора = площадь × норма раствора. Количество препарата = площадь × норма препарата."
                )
            }

            item {
                InfoCard(
                    "Замесы",
                    "Если последний замес неполный, приложение отдельно считает его объём и количество препарата для него."
                )
            }

            item {
                InfoCard(
                    "Вылеты",
                    "Количество вылетов рассчитывается по объёму бака выбранного дрона. Последний неполный бак показывается отдельно."
                )
            }

            item {
                InfoCard(
                    "АКБ",
                    "Для среднего времени введи фактические значения в формате мм:сс, например: 12:40 13:05 11:55."
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, text: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
