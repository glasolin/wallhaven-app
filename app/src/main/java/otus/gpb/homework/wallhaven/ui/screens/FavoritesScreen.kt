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
import otus.gpb.homework.wallhaven.ui.navigation.FAVORITES_ROUTE


fun NavController.navigateToFavorites(navOptions: NavOptions) = navigate(FAVORITES_ROUTE, navOptions)

fun NavGraphBuilder.favoritesScreen() {
    composable(
        route = FAVORITES_ROUTE,
    ) {
        FavoritesRoute()
    }
}
@Composable
internal fun FavoritesRoute(
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel = hiltViewModel(),
) {
    FavoritesScreen(
        modifier
    )
}

@Composable
internal fun FavoritesScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Text("FavoritesScreen!")
    }
}
