package otus.gpb.homework.wallhaven.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.rounded.Bookmarks
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

enum class Navigation(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
) {
    MAIN(
        selectedIcon = Icons.Rounded.Bookmarks,
        unselectedIcon = Icons.Outlined.Bookmarks,
        iconTextId = R.string.navigation_main,
        titleTextId = R.string.navigation_main,
    ),
    FAVORITES(
        selectedIcon = Icons.Rounded.Bookmarks,
        unselectedIcon = Icons.Outlined.Bookmarks,
        iconTextId = R.string.navigation_favorites,
        titleTextId = R.string.navigation_favorites,
    ),
    SETTINGS(
        selectedIcon = Icons.Rounded.Bookmarks,
        unselectedIcon = Icons.Outlined.Bookmarks,
        iconTextId = R.string.navigation_settings,
        titleTextId = R.string.navigation_settings,
    ),
}
