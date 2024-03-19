package otus.gpb.homework.wallhaven

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.util.trace
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import otus.gpb.homework.wallhaven.ui.navigation.FAVORITES_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.MAIN_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.Navigation
import otus.gpb.homework.wallhaven.ui.navigation.SETTINGS_ROUTE
import otus.gpb.homework.wallhaven.ui.screens.navigateToFavorites
import otus.gpb.homework.wallhaven.ui.screens.navigateToMain
import otus.gpb.homework.wallhaven.ui.screens.navigateToSettings

@Composable
fun rememberAppState(
    windowSizeClass: WindowSizeClass,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
): AppState {
    return remember(
        navController,
        coroutineScope,
        windowSizeClass,
        networkMonitor,
    ) {
        AppState(
            navController = navController,
            coroutineScope = coroutineScope,
            windowSizeClass = windowSizeClass,
        )
    }
}

class AppState(
    val navController: NavHostController,
    coroutineScope: CoroutineScope,
    val windowSizeClass: WindowSizeClass,
) {
    val destination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: Navigation?
        @Composable get() = when (destination?.route) {
            MAIN_ROUTE -> Navigation.MAIN
            FAVORITES_ROUTE -> Navigation.FAVORITES
            SETTINGS_ROUTE -> Navigation.SETTINGS
            else -> null
        }

    val shouldShowBottomBar: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val shouldShowNavRail: Boolean
        get() = !shouldShowBottomBar

    val topLevelDestinations: List<Navigation> = Navigation.entries

    /**
     * UI logic for navigating to a top level destination in the app. Top level destinations have
     * only one copy of the destination of the back stack, and save and restore state whenever you
     * navigate to and from it.
     *
     * @param topLevelDestination: The destination the app needs to navigate to.
     */
    fun navigateToTopLevelDestination(topLevelDestination: Navigation) {
        trace("Navigation: ${topLevelDestination.name}") {
            val topLevelNavOptions = navOptions {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }

            when (topLevelDestination) {
                Navigation.MAIN -> navController.navigateToMain(topLevelNavOptions)
                Navigation.FAVORITES -> navController.navigateToFavorites(topLevelNavOptions)
                Navigation.SETTINGS -> navController.navigateToSettings(topLevelNavOptions)
            }
        }
    }

}
