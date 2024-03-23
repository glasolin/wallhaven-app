package otus.gpb.homework.wallhaven.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.util.trace
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import kotlinx.coroutines.CoroutineScope
import otus.gpb.homework.wallhaven.ui.navigation.FAVORITES_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.MAIN_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.Navigation
import otus.gpb.homework.wallhaven.ui.navigation.SETTINGS_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.WALLHAVEN_SITE_ROUTE
import otus.gpb.homework.wallhaven.ui.screens.navigateToFavorites
import otus.gpb.homework.wallhaven.ui.screens.navigateToMain
import otus.gpb.homework.wallhaven.ui.screens.navigateToSettings
import javax.inject.Inject

class UiState @Inject constructor() {

    private var context:Context?=null
        get() {requireNotNull(field){println("Context was not initialized")};return field}

    var navController: NavHostController? = null
        get() {requireNotNull(field){println("Nav controller was not initialized")};return field}

    var coroutineScope: CoroutineScope? = null
        get() {requireNotNull(field){println("Coroutine scope was not initialized")};return field}

    var windowSizeClass: WindowSizeClass? = null
        get() {requireNotNull(field){println("Window Size Class scope was not initialized")};return field}

    val currentRoute: NavDestination?
        @Composable get() = navController!!.currentBackStackEntryAsState().value?.destination

    val currentScreen: Navigation?
        @Composable get() = when (currentRoute?.route) {
            MAIN_ROUTE -> Navigation.MAIN
            FAVORITES_ROUTE -> Navigation.FAVORITES
            SETTINGS_ROUTE -> Navigation.SETTINGS
            else -> null
        }

    val shouldShowBottomBar: Boolean
        get() = windowSizeClass!!.widthSizeClass == WindowWidthSizeClass.Compact

    val shouldShowNavRail: Boolean
        get() = !shouldShowBottomBar

    val screensList: List<Navigation> = Navigation.entries

    fun navigate(screen: Navigation) {
        trace("Navigation: ${screen.name}") {
            val screenOptions = navOptions {
                popUpTo(navController!!.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }

            when (screen) {
                Navigation.MAIN -> navController!!.navigateToMain(screenOptions)
                Navigation.FAVORITES -> navController!!.navigateToFavorites(screenOptions)
                Navigation.SETTINGS -> navController!!.navigateToSettings(screenOptions)
            }
        }
    }

    fun reloadMainGrid() {
        TODO("Not yet implemented")
    }

    fun navigateBack() {
        TODO("Not yet implemented")
    }

    //@Composable
    fun openWallhavenSite() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WALLHAVEN_SITE_ROUTE)).also{
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context!!.startActivity(intent)
    }

    fun setContext(context: Context) {
        this.context=context
    }

}
