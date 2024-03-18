package otus.gpb.homework.wallhaven.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.ui.navigation.MAIN_ROUTE


fun NavController.navigateToMain(navOptions: NavOptions) = navigate(MAIN_ROUTE, navOptions)

fun NavGraphBuilder.mainScreen(onTopicClick: (String) -> Unit) {
    composable(
        route = MAIN_ROUTE,
    ) {
        MainRoute(onTopicClick)
    }
}
@Composable
internal fun MainRoute(
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel = hiltViewModel(),
) {
    MainScreen(
        modifier
    )
}

@Composable
internal fun MainScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    )
}
