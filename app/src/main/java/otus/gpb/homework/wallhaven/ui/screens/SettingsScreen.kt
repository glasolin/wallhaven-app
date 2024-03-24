package otus.gpb.homework.wallhaven.ui.screens

import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialogDefaults.shape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
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
import androidx.compose.ui.platform.LocalContext
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
import otus.gpb.homework.wallhaven.ui.StoreDataTypes
import otus.gpb.homework.wallhaven.ui.assets.DropdownMenuBox
import otus.gpb.homework.wallhaven.ui.assets.PieChart
import otus.gpb.homework.wallhaven.ui.theme.AppIcons
import otus.gpb.homework.wallhaven.ui.theme.AppTheme
import otus.gpb.homework.wallhaven.ui.theme.Colors
import otus.gpb.homework.wallhaven.ui.theme.LocalStoragePieChartColors
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
        settings=viewModel.settings(),
        data=viewModel.data(),
        modifier= Modifier
            .padding(all = 10.dp)
            //.scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
            .verticalScroll(rememberScrollState())
    )
}

@Preview
@Composable
private fun PreviewSettingsScreen() {
    AppTheme {
        SettingsScreen(
            modifier = Modifier,
            settings= Settings(),
            data= UiData().apply { setContext(LocalContext.current) },
        )
    }
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
        var selectedPieItem by remember { mutableStateOf(StoreDataTypes.NONE) }

        val pieData=mapOf(
            StoreDataTypes.FAVORITES to Triple(
                stringResource(R.string.settings_storage_favorites),
                data.storeUsage.value[StoreDataTypes.FAVORITES]!!.toFloat(),
                LocalStoragePieChartColors.current.colorFavorites,
            ),
            StoreDataTypes.CACHE to Triple(
                stringResource(R.string.settings_storage_cache),
                data.storeUsage.value[StoreDataTypes.CACHE]!!.toFloat(),
                LocalStoragePieChartColors.current.colorCache,
            ),
            StoreDataTypes.FREE to Triple(
                stringResource(R.string.settings_storage_free),
                data.storeUsage.value[StoreDataTypes.FREE]!!.toFloat(),
                LocalStoragePieChartColors.current.colorFree,
            )
        )
        PieChart<StoreDataTypes>(
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
                Column(modifier = Modifier.weight(0.1f)) {
                    Box(
                        modifier = Modifier
                            .size(boxHeight)
                            .clip(RectangleShape)
                            .background(color)
                    )
                }
                Column(modifier = Modifier.weight(0.5f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Column(modifier = Modifier.weight(0.4f)) {
                    Text(
                        text = data.bytesToHuman(value.toLong()),
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
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
            modifier= Modifier
                .align(Alignment.End)
                .padding(top = 24.dp, bottom = 8.dp),
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


