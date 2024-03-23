package otus.gpb.homework.wallhaven.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import otus.gpb.homework.wallhaven.ui.screens.favoritesScreen
import otus.gpb.homework.wallhaven.ui.screens.mainScreen
import otus.gpb.homework.wallhaven.ui.screens.settingsScreen
import otus.gpb.homework.wallhaven.ui.UiState

@Composable
fun AppNavHost(
    state: UiState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    startRoute: String = MAIN_ROUTE,
) {
    NavHost(
        navController = state.navController!!,
        startDestination = startRoute,
        modifier = modifier,
    ) {
        mainScreen()
        favoritesScreen()
        settingsScreen()
    }
}