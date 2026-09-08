package com.agrofront.agrohelper.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrofront.agrohelper.ui.components.AgroActionCard

@Composable
fun HomeScreen(
    onNewWork: () -> Unit,
    onCalculators: () -> Unit,
    onEquipment: () -> Unit,
    onJournal: () -> Unit,
    onPlaceholder: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "АгроФронт",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Помощник оператора агродронов",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Расчёты, техника и рабочие данные — в одном приложении.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            Text(
                text = "Основное",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            AgroActionCard(
                title = "Новая работа",
                subtitle = "Подготовка новой обработки",
                icon = Icons.Default.Flight
            ,
                onClick = onNewWork
            )
        }

        item {
            AgroActionCard(
                title = "Калькуляторы",
                subtitle = "Раствор, препарат, замесы, вылеты, АКБ",
                icon = Icons.Default.Calculate,
                onClick = onCalculators
            )
        }

        item {
            AgroActionCard(
                title = "Техника",
                subtitle = "DJI, XAG и HD / Huida Tech",
                icon = Icons.Default.Inventory2,
                onClick = onEquipment
            )
        }

        item {
            Text(
                text = "Данные",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            AgroActionCard(
                title = "Поля",
                subtitle = "Площади и участки",
                icon = Icons.Default.Map
            ) { onPlaceholder("Поля") }
        }

        item {
            AgroActionCard(
                title = "Журнал работ",
                subtitle = "История выполненных обработок",
                icon = Icons.Default.EditNote
            ,
                onClick = onJournal
            )
        }

        item {
            AgroActionCard(
                title = "Справочники",
                subtitle = "Полезные данные и подсказки",
                icon = Icons.Default.MenuBook
            ) { onPlaceholder("Справочники") }
        }

        item {
            AgroActionCard(
                title = "Настройки",
                subtitle = "Параметры приложения",
                icon = Icons.Default.Settings
            ) { onPlaceholder("Настройки") }
        }

        item { Column(modifier = Modifier.padding(bottom = 16.dp)) {} }
    }
}
