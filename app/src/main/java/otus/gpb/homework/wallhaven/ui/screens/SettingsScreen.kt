package otus.gpb.homework.wallhaven.ui.screens

import android.content.res.Resources
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.Settings
import otus.gpb.homework.wallhaven.Themes
import otus.gpb.homework.wallhaven.ui.navigation.SETTINGS_ROUTE
import otus.gpb.homework.wallhaven.ui.theme.AppTheme


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
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel = hiltViewModel(),
) {
    SettingsScreen(
        modifier=modifier,
        settings=viewModel.settings(),
    )
}

@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    settings: Settings
) {
    Column {
        SettingsSectionTitle(stringResource(R.string.settings_theme_section))
        Column(Modifier.selectableGroup()) {
            SettingsThemeChooserRow(
                text = stringResource(R.string.settings_theme_system),
                selected = settings.theme == Themes.SYSTEM,
                onClick = { },
            )
            SettingsThemeChooserRow(
                text = stringResource(R.string.settings_theme_light),
                selected = settings.theme == Themes.LIGHT,
                onClick = {settings.theme = Themes.LIGHT },
            )
            SettingsThemeChooserRow(
                text = stringResource(R.string.settings_theme_dark),
                selected = settings.theme == Themes.DARK,
                onClick = { },
            )
        }
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
        )
    }
}