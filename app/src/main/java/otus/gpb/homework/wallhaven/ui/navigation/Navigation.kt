package otus.gpb.homework.wallhaven.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.ui.screens.MainRoute
const val MAIN_ROUTE = "main_route"
const val SETTINGS_ROUTE = "settings_route"
const val FAVORITES_ROUTE = "favorites_route"
const val FILTERS_ROUTE = "filters_route"
const val IMAGE_ROUTE = "image_route"
const val WALLHAVEN_SITE_ROUTE = "https://wallhaven.cc"
enum class TitleBarItems {
    RELOAD, TITLE, SITE, BACK, DYNAMIC_TITLE
}

enum class Navigation(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
    val titleBarItemsIds: List<TitleBarItems>,
    val visible:Boolean=true,
) {
    MAIN(
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home,
        iconTextId = R.string.navigation_main,
        titleTextId = R.string.navigation_main,
        titleBarItemsIds = listOf(TitleBarItems.RELOAD,TitleBarItems.DYNAMIC_TITLE,TitleBarItems.SITE),
    ),
    FAVORITES(
        selectedIcon = Icons.Rounded.Bookmarks,
        unselectedIcon = Icons.Outlined.Bookmarks,
        iconTextId = R.string.navigation_favorites,
        titleTextId = R.string.navigation_favorites,
        titleBarItemsIds = listOf(TitleBarItems.TITLE,TitleBarItems.SITE)
    ),
    SETTINGS(
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        iconTextId = R.string.navigation_settings,
        titleTextId = R.string.navigation_settings,
        titleBarItemsIds = listOf(TitleBarItems.TITLE,TitleBarItems.SITE),
    ),
    FILTERS(
        selectedIcon = Icons.Rounded.FilterAlt,
        unselectedIcon = Icons.Outlined.FilterAlt,
        iconTextId = R.string.navigation_filters,
        titleTextId = R.string.navigation_filters,
        titleBarItemsIds = listOf(TitleBarItems.TITLE,TitleBarItems.BACK),
        visible = false,
    ),
    IMAGE(
        selectedIcon = Icons.Rounded.Image,
        unselectedIcon = Icons.Outlined.Image,
        iconTextId = R.string.navigation_image,
        titleTextId = R.string.navigation_image,
        titleBarItemsIds = listOf(TitleBarItems.DYNAMIC_TITLE,TitleBarItems.BACK),
        visible = false,
    ),
}
