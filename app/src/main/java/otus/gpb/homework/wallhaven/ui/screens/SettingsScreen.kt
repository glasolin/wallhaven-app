package otus.gpb.homework.wallhaven.ui.screens

import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialogDefaults.shape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.Settings
import otus.gpb.homework.wallhaven.Themes
import otus.gpb.homework.wallhaven.ui.UiData
import otus.gpb.homework.wallhaven.ui.navigation.SETTINGS_ROUTE
import otus.gpb.homework.wallhaven.ui.storeDataTypes
import otus.gpb.homework.wallhaven.ui.theme.AppIcons
import otus.gpb.homework.wallhaven.ui.theme.AppTheme
import otus.gpb.homework.wallhaven.ui.theme.Colors
import otus.gpb.homework.wallhaven.ui.theme.LocalStoragePieChartColors
import otus.gpb.homework.wallhaven.ui.theme.StoragePieChartLightColors
import kotlin.math.min


fun NavController.navigateToSettings(navOptions: NavOptions) = navigate(SETTINGS_ROUTE, navOptions)

fun NavGraphBuilder.settingsScreen() {
    composable(
        route = SETTINGS_ROUTE,
    ) {
        SettingsRoute()
    }
}
@Composable
internal fun SettingsRoute(
    viewModel: MainActivityViewModel = hiltViewModel(),
) {
    SettingsScreen(
        modifier=Modifier.padding(all=10.dp),
        settings=viewModel.settings(),
        data=viewModel.data(),
    )
}

@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    settings: Settings,
    data: UiData,
) {
    Column(modifier=modifier) {
        SettingsSectionTitle(stringResource(R.string.settings_api_key_section))
        OutlinedTextField(
            value = settings.apiKey.observeAsState().value!!,
            singleLine = true,
            onValueChange = {settings.apiKey.value=it },
            label = { Text(stringResource(R.string.settings_api_key_label)) }
        )

        SettingsSectionTitle(stringResource(R.string.settings_theme_section))
        DropdownMenuBox<Themes>(
            items = mapOf(
                Themes.SYSTEM to stringResource(R.string.settings_theme_system),
                Themes.LIGHT to stringResource(R.string.settings_theme_light),
                Themes.DARK to stringResource(R.string.settings_theme_dark),
            ),
            selected = settings.theme.observeAsState().value!!,
            onSelect = {s->settings.theme.value=s},
        )

        SettingsSectionTitle(stringResource(R.string.settings_storage_section))
        var selectedPieItem by remember { mutableStateOf(storeDataTypes.NONE) }

        val pieData=mapOf(
            storeDataTypes.FAVORITES to Triple(
                stringResource(R.string.settings_storage_favorites),
                data.storeUsage.value[storeDataTypes.FAVORITES]!!.toFloat(),
                LocalStoragePieChartColors.current.colorFavorites,
            ),
            storeDataTypes.CACHE to Triple(
                stringResource(R.string.settings_storage_cache),
                data.storeUsage.value[storeDataTypes.CACHE]!!.toFloat(),
                LocalStoragePieChartColors.current.colorCache,
            ),
            storeDataTypes.FREE to Triple(
                stringResource(R.string.settings_storage_free),
                data.storeUsage.value[storeDataTypes.FREE]!!.toFloat(),
                LocalStoragePieChartColors.current.colorFree,
            )
        )
        PieChart<storeDataTypes>(
            modifier= Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterHorizontally),
            data = pieData,
            selected = selectedPieItem,
            onSelect = {s-> selectedPieItem = s}
        )
        val boxHeight: Dp = with(LocalDensity.current) {
            MaterialTheme.typography.titleMedium.fontSize.toDp()
        }
        pieData.forEach() { (id, item) ->
            val (name,value,color)=item
            Row(modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(0.6f)
                .align(Alignment.CenterHorizontally)
            ) {
                Column(modifier = Modifier.weight(0.2f)) {
                    Box(
                        modifier = Modifier
                            .size(boxHeight)
                            .clip(RectangleShape)
                            .background(color)
                    )
                }
                Column(modifier = Modifier.weight(0.6f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 20.dp)
                    )
                }
                Column(modifier = Modifier.weight(0.2f)) {
                    Text(
                        text = value.toInt().toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = {data.clearStorage()},
            icon = { Icon(AppIcons.ClearCache,"") },
            text={ Text(stringResource(R.string.settings_storage_button_clear)) },
            modifier= Modifier
                .align(Alignment.End)
                .padding(top = 24.dp),
        )
    }
}
@Composable
internal fun ThemesRadio(settings: Settings) {
SettingsSectionTitle(stringResource(R.string.settings_theme_section))
    Column(Modifier.selectableGroup()) {
        SettingsThemeChooserRow(
            text = stringResource(R.string.settings_theme_system),
            selected = settings.theme.value == Themes.SYSTEM,
            onClick = { settings.theme.value = Themes.SYSTEM },
        )
        SettingsThemeChooserRow(
            text = stringResource(R.string.settings_theme_light),
            selected = settings.theme.value == Themes.LIGHT,
            onClick = { settings.theme.value = Themes.LIGHT },
        )
        SettingsThemeChooserRow(
            text = stringResource(R.string.settings_theme_dark),
            selected = settings.theme.value == Themes.DARK,
            onClick = { settings.theme.value = Themes.DARK },
        )
    }
}
@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Preview
@Composable
private fun PreviewSettingsSecreen() {
    AppTheme {
        SettingsScreen(
            modifier = Modifier,
            settings= Settings(),
            data= UiData(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownMenuBox(
    items: Map<T,String>,
    selected: T,
    onSelect: (selectedItem: T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(selected) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = items[selected]!!,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { (id, item) ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            selectedItem=id
                            expanded = false
                            onSelect(id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun <T> PieChart(
    modifier: Modifier = Modifier,
    data: Map<T,Triple<String, Float, Color>>,
    animated: Boolean = true,
    selected: T,
    onSelect: (selectedItem: T) -> Unit,
) {
    val chartDegrees = 360f // circle shape
    var startAngle = 270f // top to right

    val colors = mutableListOf<Color>()
    val names = mutableListOf<String>()
    val values= mutableListOf<Float>()

    data.forEach() { (id,item) ->
        val (name,value,color)=item
        colors.add(color)
        names.add(name)
        values.add(value)
    }

    var total=values.sum()
    val proportions = values.map {v ->
        v * 100 / total
    }

    // calculate each input slice degrees
    val angleProgress = proportions.map {v ->
        chartDegrees * v / 100
    }

    // calculate each slice end point in degrees, for handling click position
    val progressSize = mutableListOf<Float>()

    LaunchedEffect(angleProgress){
        progressSize.add(angleProgress.first())
        for (x in 1 until angleProgress.size) {
            progressSize.add(angleProgress[x] + progressSize[x - 1])
        }
    }
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {

        val canvasSize = min(constraints.maxWidth, constraints.maxHeight)
        val size = Size(canvasSize.toFloat(), canvasSize.toFloat())
        val canvasSizeDp = with(LocalDensity.current) { canvasSize.toDp() }

        Canvas(modifier = Modifier.size(canvasSizeDp)) {

            angleProgress.forEachIndexed { index, angle ->
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = angle,
                    useCenter = true,
                    size = size,
                    style = Fill
                )
                startAngle += angle
            }
        }
    }
}

private fun Int.textDp(density: Density): TextUnit = with(density) {
    this@textDp.dp.toSp()
}

val Int.textDp: TextUnit
    @Composable get() =  this.textDp(density = LocalDensity.current)