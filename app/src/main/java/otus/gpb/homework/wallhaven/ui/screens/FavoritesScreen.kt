package otus.gpb.homework.wallhaven.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import coil.compose.rememberAsyncImagePainter
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.ui.UiData
import otus.gpb.homework.wallhaven.ui.UiState
import otus.gpb.homework.wallhaven.ui.navigation.FAVORITES_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.Navigation
import otus.gpb.homework.wallhaven.ui.theme.AppIcons
import otus.gpb.homework.wallhaven.ui.theme.AppTheme
import otus.gpb.homework.wallhaven.wh.ImageInfo
import otus.gpb.homework.wallhaven.wh.WHFileType
import otus.gpb.homework.wallhaven.wh.WHPageStatus
import otus.gpb.homework.wallhaven.wh.WHStatus
import otus.gpb.homework.wallhaven.wh.WH_THUMB_MAX_DIMENTION
import java.io.File


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
    val images=data.favoritesData.collectAsState().value
    Column(modifier=modifier) {
        if (images.isNotEmpty()) {
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
    val images=data.favoritesData.collectAsState().value
    when (images.isEmpty()) {
        true -> {
            Text("No data")
        }
        else -> {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(WH_THUMB_MAX_DIMENTION.dp),
                verticalItemSpacing = 2.dp,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                content = {
                    items(count = images.size) { idx ->
                        ShowFavoriteThumbnail(data = data, image = images[idx], state = state)
                        }
                    },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
internal fun ShowFavoriteThumbnail(
    data:UiData,
    state: UiState,
    image: ImageInfo,
    modifier: Modifier = Modifier,
) {

    val painter =
        rememberAsyncImagePainter(model = File(data.imageFromFavorite(image.id, WHFileType.THUMBNAIL)))

    Image(
        painter = painter,
        contentDescription = "",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .width(image.thumbWidth.dp)
            .height(image.thumbHeight.dp)
            .clickable(true, null) {
                data.selectFavouriteImage(image)
                state.navigate(Navigation.IMAGE)
            }
    )
}