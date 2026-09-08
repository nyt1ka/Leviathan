package com.agrofront.agrohelper.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.agrofront.agrohelper.data.AppSettings

@Composable
fun SettingsScreen(
    initial: AppSettings,
    onBack: () -> Unit,
    onSave: (AppSettings) -> Unit
) {
    var mixTank by remember { mutableStateOf(initial.defaultMixTankLiters.toString()) }
    var solutionRate by remember { mutableStateOf(initial.defaultSolutionRateLHa.toString()) }
    var rememberDrone by remember { mutableStateOf(initial.rememberLastDrone) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "Значения по умолчанию",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                OutlinedTextField(
                    value = mixTank,
                    onValueChange = { mixTank = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ёмкость для замеса, л") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            item {
                OutlinedTextField(
                    value = solutionRate,
                    onValueChange = { solutionRate = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Норма раствора, л/га") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Запоминать последний дрон")
                        Text(
                            "Ускоряет создание следующей работы",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = rememberDrone,
                        onCheckedChange = { rememberDrone = it }
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        onSave(
                            AppSettings(
                                defaultMixTankLiters = mixTank.replace(',', '.').toDoubleOrNull() ?: 500.0,
                                defaultSolutionRateLHa = solutionRate.replace(',', '.').toDoubleOrNull() ?: 10.0,
                                rememberLastDrone = rememberDrone
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить настройки")
                }
            }
        }
    }
}
