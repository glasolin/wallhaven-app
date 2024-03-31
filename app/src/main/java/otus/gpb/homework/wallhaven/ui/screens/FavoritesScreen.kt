package otus.gpb.homework.wallhaven.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.ui.UiData
import otus.gpb.homework.wallhaven.ui.UiState
import otus.gpb.homework.wallhaven.ui.navigation.FAVORITES_ROUTE
import otus.gpb.homework.wallhaven.ui.theme.AppIcons
import otus.gpb.homework.wallhaven.ui.theme.AppTheme
import otus.gpb.homework.wallhaven.wh.WHLoadingStatus
import otus.gpb.homework.wallhaven.wh.WHStatus
import otus.gpb.homework.wallhaven.wh.WH_THUMB_MAX_DIMENTION


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
    viewModel.data().favoritesList()
    FavoritesScreen(
        data=viewModel.data(),
        state=viewModel.state(),
        modifier=modifier
            .padding(all = 10.dp),
        )
}

@Composable
internal fun FavoritesScreen(
    data:UiData,
    state:UiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier=modifier) {
        if (data.imagesTotal.intValue > 0) {
            ExtendedFloatingActionButton(
                onClick = { data.clearFavorites() },
                icon = { Icon(AppIcons.clearFavorites, "") },
                text = { Text(stringResource(R.string.image_button_clear_favorites)) },
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 24.dp, bottom = 8.dp),
            )
        }
        FavoritesMainGrid(
            data = data,
            state = state,
        )
    }
}

@Preview
@Composable
private fun PreviewFavoritesScreen() {
    AppTheme {
        FavoritesScreen(
            modifier = Modifier,
            state = UiState(),
            data= UiData().apply { setContext(LocalContext.current) },
        )
    }
}


@Composable
internal fun FavoritesMainGrid(
    data: UiData,
    state:UiState,
    modifier: Modifier = Modifier,
) {
    val tag="FavoritesMainGrid"
    when (val total=data.imagesTotal.intValue) {
        0 -> {
            Text("No data")
        }
        -1 -> {
            Text("Not loaded")
        }
        else -> {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(WH_THUMB_MAX_DIMENTION.dp),
                verticalItemSpacing = 2.dp,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                content = {
                    items(count = total) { idx ->
                        if (data.imagesData[idx] == null) {
                            ShowGalleryPlaceholder()
                        } else {
                            when (data.imagesData[idx]!!.thumbStatus) {
                                WHStatus.NONE -> ShowGalleryPlaceholder()
                                WHStatus.INFO, WHStatus.LOADING,WHStatus.ERROR -> ShowThumbnailPlaceholder(data.imagesData[idx]!!)
                                WHStatus.LOADED -> ShowThumbnail(data = data, image = data.imagesData[idx]!!, state = state)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}