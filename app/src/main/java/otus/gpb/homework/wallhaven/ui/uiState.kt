package otus.gpb.homework.wallhaven.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.util.trace
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.ui.navigation.FAVORITES_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.FILTERS_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.IMAGE_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.MAIN_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.Navigation
import otus.gpb.homework.wallhaven.ui.navigation.SETTINGS_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.WALLHAVEN_SITE_ROUTE
import otus.gpb.homework.wallhaven.ui.screens.navigateToFavorites
import otus.gpb.homework.wallhaven.ui.screens.navigateToFilters
import otus.gpb.homework.wallhaven.ui.screens.navigateToImage
import otus.gpb.homework.wallhaven.ui.screens.navigateToMain
import otus.gpb.homework.wallhaven.ui.screens.navigateToSettings
import javax.inject.Inject

class UiState constructor() {
    private var context:Context?=null
        get() {requireNotNull(field){println("Context was not initialized")};return field}

    var navController: NavHostController? = null
        get() {requireNotNull(field){println("Nav controller was not initialized")};return field}

    var windowSizeClass: WindowSizeClass? = null
        get() {requireNotNull(field){println("Window Size Class scope was not initialized")};return field}

    val currentRoute: NavDestination?
        @Composable get() = navController!!.currentBackStackEntryAsState().value?.destination

    var dynamicTitle = mutableStateOf<String>("")

    fun setDynamicTitle(title:String) {
        dynamicTitle.value=title
    }

    val currentScreen: Navigation?
        @Composable get() = when (currentRoute?.route) {
            MAIN_ROUTE -> Navigation.MAIN
            FAVORITES_ROUTE -> Navigation.FAVORITES
            SETTINGS_ROUTE -> Navigation.SETTINGS
            FILTERS_ROUTE -> Navigation.FILTERS
            IMAGE_ROUTE -> Navigation.IMAGE
            else -> null
        }

    val shouldShowBottomBar: Boolean
        get() = windowSizeClass!!.widthSizeClass == WindowWidthSizeClass.Compact

    val shouldShowNavRail: Boolean
        get() = !shouldShowBottomBar

    val screensList: List<Navigation> = Navigation.entries

    fun setContext(context: Context) {
        this.context=context
    }

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
                Navigation.FILTERS -> navController!!.navigateToFilters(screenOptions)
                Navigation.IMAGE -> navController!!.navigateToImage(screenOptions)
            }
        }
    }

    fun navigateBack() {
        navController!!.popBackStack()
        //navController!!.navigateUp()
    }

    //@Composable
    fun openWallhavenSite() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WALLHAVEN_SITE_ROUTE)).also{
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context!!.startActivity(intent)
    }
}
