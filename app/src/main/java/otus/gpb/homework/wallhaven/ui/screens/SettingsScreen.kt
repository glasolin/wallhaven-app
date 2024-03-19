package otus.gpb.homework.wallhaven.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.ui.navigation.SETTINGS_ROUTE


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
        modifier
    )
}

@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Text("SettingsScreen!")
    }
}
