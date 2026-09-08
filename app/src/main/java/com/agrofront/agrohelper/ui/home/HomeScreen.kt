package com.agrofront.agrohelper.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrofront.agrohelper.ui.components.AgroActionCard

data class HomeEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun HomeScreen(
    onFlightJournal: () -> Unit,
    onCalculators: () -> Unit,
    onJournal: () -> Unit,
    onFields: () -> Unit,
    onReferences: () -> Unit,
    onCadastralMap: () -> Unit,
    onSettings: () -> Unit
) {
    val entries = listOf(
        HomeEntry("Журнал полётов", "Расчёт и подготовка обработки", Icons.Default.FlightTakeoff, onFlightJournal),
        HomeEntry("Калькуляторы", "Раствор, препарат, замесы и время", Icons.Default.Calculate, onCalculators),
        HomeEntry("Поля", "Площади, участки и заметки", Icons.Default.Landscape, onFields),
        HomeEntry("Журнал работ", "История выполненных обработок", Icons.Default.EditNote, onJournal),
        HomeEntry("Справочники", "Агрономия, техника и полезные материалы", Icons.Default.MenuBook, onReferences),
        HomeEntry("Кадастровая карта", "Публичная кадастровая карта РФ", Icons.Default.Map, onCadastralMap),
        HomeEntry("Настройки", "Параметры AgroHelper", Icons.Default.Settings, onSettings)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columns = if (maxWidth >= 840.dp) 3 else if (maxWidth >= 600.dp) 2 else 1
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(span = { GridItemSpan(columns) }) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(34.dp),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.92f)
                                    )
                                )
                            )
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("AgroHelper", fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary)
                        Text("Мобильный офлайн помощник дроновода", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                        Text(
                            "dev alpha 0.2 · расчёты, поля, справочники и рабочие данные",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f)
                        )
                    }
                }
            }

            item(span = { GridItemSpan(columns) }) {
                Text(
                    text = "Рабочее пространство",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(entries, key = { it.title }) { entry ->
                AgroActionCard(entry.title, entry.subtitle, entry.icon, entry.action)
            }

            item(span = { GridItemSpan(columns) }) {
                Column(modifier = Modifier.padding(bottom = 18.dp)) {}
            }
        }
    }
}
