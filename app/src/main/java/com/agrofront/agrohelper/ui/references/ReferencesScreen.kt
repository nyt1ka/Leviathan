package com.agrofront.agrohelper.ui.references

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrofront.agrohelper.ui.components.AgroActionCard
import com.agrofront.agrohelper.ui.components.AgroSectionCard

data class ReferenceArticle(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val body: String
)

private val articles = listOf(
    ReferenceArticle(
        "Защита растений",
        "Как читать регламент обработки и что проверять перед вылетом",
        Icons.Default.Spa,
        """Рабочий алгоритм: культура → фаза → вредный объект → зарегистрированный препарат → норма применения → срок ожидания → число разрешённых обработок.\n\nНормы и ограничения обязательно сверяются с действующей этикеткой препарата и Государственным каталогом пестицидов и агрохимикатов. Если в источнике нет значения, его нельзя заменять «средним по отрасли».\n\nПеред обработкой проверьте культуру, фазу, погоду, технику, норму вылива и совместимость компонентов баковой смеси."""
    ),
    ReferenceArticle(
        "Размер капли и норма вылива",
        "Практический ориентир для УМО-обработки агродроном",
        Icons.Default.Opacity,
        """Размер капли выбирают по типу препарата, требуемому покрытию и риску сноса.\n\nПрактические диапазоны, приведённые AGI Systems: фунгициды — примерно 50–150 мкм и 10–15 л/га; инсектициды — 80–200 мкм и 10–15 л/га; контактные гербициды — 100–200 мкм и 10–15 л/га; системные гербициды — 150–250 мкм и 5–8 л/га.\n\nЭто справочный ориентир, а не замена этикетке препарата и решению агронома."""
    ),
    ReferenceArticle(
        "Баковые смеси",
        "Совместимость, порядок смешивания и контроль раствора",
        Icons.Default.Science,
        """Перед приготовлением смеси проверяют физико-химическую совместимость препаратов и ограничения производителя. Для незнакомой комбинации делают пробный тест в небольшом объёме.\n\nВода должна быть пригодной для конкретного препарата. Компоненты добавляют последовательно, обеспечивая перемешивание. После каждого компонента контролируют отсутствие хлопьев, осадка, пены и расслоения.\n\nНе храните готовую смесь дольше допустимого времени и не используйте неподтверждённую совместимость как норму."""
    ),
    ReferenceArticle(
        "Болезни, вредители и сорняки",
        "Что учитывать при выборе защиты",
        Icons.Default.BugReport,
        """Определение вредного объекта — первый шаг к выбору препарата. Одна и та же культура может иметь несколько болезней, вредителей и групп сорняков с разными регламентами.\n\nФиксируйте культуру, фазу развития, локализацию симптомов и масштаб поражения. При сомнении диагноз лучше подтвердить специалистом до обработки.\n\nВ AgroHelper этот раздел используется как памятка; подбор конкретного зарегистрированного препарата всегда требует проверки актуального реестра."""
    ),
    ReferenceArticle(
        "Разбрасывание и калибровка",
        "Гранулы, семена и удобрения",
        Icons.Default.Grain,
        """Перед работой разбрасыватель калибруют под конкретный материал. На расход влияют фракция, влажность, плотность и состояние материала.\n\nПосле контрольного прохода остаток взвешивают, фактический расход делят на обработанную площадь и при необходимости корректируют коэффициент. При смене партии материала калибровку повторяют.\n\nНорму внесения задаёт агрономическая задача, а не максимальная производительность оборудования."""
    ),
    ReferenceArticle(
        "Устройство агродрона",
        "Основные узлы, которые нужно понимать оператору",
        Icons.Default.PrecisionManufacturing,
        """Основные группы: планер и силовая установка; бак, насосы, магистрали, фильтры и распылители; система разбрасывания; навигация и RTK; инерциальные датчики; радары и системы обнаружения препятствий; полевая энергетика и зарядная станция.\n\nНисходящий поток винтов влияет на проникновение капли в полог. Загрязнение датчиков и радаров может ухудшить следование рельефу. Отказ зарядной системы способен остановить всю смену, поэтому энергетика — часть производственной цепочки."""
    ),
    ReferenceArticle(
        "Производительность",
        "Почему паспортные га/ч нельзя сравнивать напрямую",
        Icons.Default.Speed,
        """Производительность зависит от нормы вылива, ширины захвата, скорости, высоты, размера поля, логистики заправки и зарядки. Цифры разных моделей корректно сравнивать только при одинаковых условиях.\n\nСправочные ёмкости из материалов AGI Systems: AGRAS T25 — бак 20 л; T40 — 40 л; T55 — 50 л; T70P — 70 л; T100 — 100 л. Фактическая выработка в поле обычно отличается от стендового максимума."""
    ),
    ReferenceArticle(
        "Документы оператора",
        "Что должно быть подготовлено до работы",
        Icons.Default.Description,
        """Комплект документов зависит от режима эксплуатации и региона. Как минимум оператору нужно заранее проверить регистрацию борта, документы на эксплуатацию и пилота, правовой режим территории, согласование использования воздушного пространства и документы на применяемые препараты.\n\nЖурнал обработок рекомендуется вести с датой и временем, участком, культурой, препаратом, нормой, погодой, бортом и оператором."""
    ),
    ReferenceArticle(
        "Безопасность полётов",
        "Погодные условия, препятствия и подготовка площадки",
        Icons.Default.Security,
        """До запуска проверяют маршрут, высоту препятствий, линии электропередачи, людей и технику рядом с зоной работ. Рабочее окно выбирают с учётом ветра, температуры, осадков и требований препарата.\n\nПеред сменой контролируют раму, лучи, винты, крепления, бак, магистрали, распылители, аккумуляторы, зарядную станцию, связь, RTK и датчики. После смены технику промывают и осматривают."""
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferencesScreen(
    onBack: () -> Unit,
    onEquipment: () -> Unit
) {
    var selected by remember { mutableStateOf<ReferenceArticle?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Справочники") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AgroSectionCard(
                    title = "База знаний AgroHelper",
                    subtitle = "Материалы адаптированы для мобильной работы в поле",
                    icon = Icons.Default.MenuBook
                ) {
                    Text(
                        "Справочная часть собрана по материалам AGI Systems / AGI Агроном и эксплуатационным источникам. Для юридически значимых норм и регламентов всегда проверяйте актуальную этикетку и официальный реестр.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                AgroActionCard(
                    title = "Техника",
                    subtitle = "Каталог DJI, XAG и HD / Huida Tech",
                    icon = Icons.Default.Agriculture,
                    onClick = onEquipment
                )
            }

            items(articles, key = { it.title }) { article ->
                AgroActionCard(article.title, article.subtitle, article.icon) { selected = article }
            }

            item { Column(modifier = Modifier.padding(bottom = 18.dp)) {} }
        }
    }

    selected?.let { article ->
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Закрыть") } },
            title = { Text(article.title, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Icon(article.icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    item {
                        Text(article.body, style = MaterialTheme.typography.bodyLarge)
                    }
                    item {
                        Text(
                            "Источник для справочной адаптации: agi-systems.ru/agronom/ и связанные обучающие материалы AGI Systems.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
    }
}
