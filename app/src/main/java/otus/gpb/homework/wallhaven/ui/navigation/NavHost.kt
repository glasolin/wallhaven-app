package otus.gpb.homework.wallhaven.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.CoroutineScope
import otus.gpb.homework.wallhaven.ui.screens.favoritesScreen
import otus.gpb.homework.wallhaven.ui.screens.mainScreen
import otus.gpb.homework.wallhaven.ui.screens.settingsScreen
import otus.gpb.homework.wallhaven.UiState

@Composable
fun AppNavHost(
    state: UiState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    startDestination: String = MAIN_ROUTE,
) {
    NavHost(
        navController = state.navController!!,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        mainScreen()
        favoritesScreen()
        settingsScreen()
    }
}