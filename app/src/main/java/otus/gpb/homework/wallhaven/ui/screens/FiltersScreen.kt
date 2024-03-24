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
import otus.gpb.homework.wallhaven.ui.navigation.FILTERS_ROUTE


fun NavController.navigateToFilters(navOptions: NavOptions) = navigate(FILTERS_ROUTE, navOptions)

fun NavGraphBuilder.filtersScreen() {
    composable(
        route = FILTERS_ROUTE,
    ) {
        FiltersRoute()
    }
}
@Composable
internal fun FiltersRoute(
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel = hiltViewModel(),
) {
    FiltersScreen(
        modifier
    )
}

@Composable
internal fun FiltersScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        /*modifier = modifier
            .fillMaxSize(),*/
    ) {
        Text("FiltersScreen!")
    }
}
