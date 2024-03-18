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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import otus.gpb.homework.wallhaven.ui.screens.MainRoute
import otus.gpb.homework.wallhaven.ui.screens.MainScreen
import otus.gpb.homework.wallhaven.ui.screens.mainScreen

class AppMainState(
    val navController: NavHostController,
    coroutineScope: CoroutineScope,
    val windowSizeClass: WindowSizeClass,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val topDestination: Navigation?
        @Composable get() = when (currentDestination?.route) {
            MAIN_ROUTE -> Navigation.MAIN
            FAVORITES_ROUTE -> Navigation.FAVORITES
            SETTINGS_ROUTE -> Navigation.SETTINGS
            else -> null
        }

    val shouldShowBottomBar: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val shouldShowNavRail: Boolean
        get() = !shouldShowBottomBar
}
@Composable
fun AppNavHost(
    appState: AppMainState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    startDestination: String = MAIN_ROUTE,
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        this.mainScreen {  }
    }
}