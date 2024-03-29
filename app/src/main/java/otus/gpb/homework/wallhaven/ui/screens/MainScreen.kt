package otus.gpb.homework.wallhaven.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.asIntState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Predicate.not
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.Settings
import otus.gpb.homework.wallhaven.ui.UiData
import otus.gpb.homework.wallhaven.ui.UiState
import otus.gpb.homework.wallhaven.ui.assets.DrawableButton
import otus.gpb.homework.wallhaven.ui.assets.DropdownMenuBox
import otus.gpb.homework.wallhaven.ui.navigation.MAIN_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.Navigation
import otus.gpb.homework.wallhaven.ui.theme.AppIcons
import otus.gpb.homework.wallhaven.ui.theme.AppTheme
import otus.gpb.homework.wallhaven.wh.WHOrder
import otus.gpb.homework.wallhaven.wh.WHSorting
import coil.compose.AsyncImage
import otus.gpb.homework.wallhaven.ui.theme.Colors
import otus.gpb.homework.wallhaven.ui.theme.LocalGalleryColors
import otus.gpb.homework.wallhaven.wh.WHFileType
import otus.gpb.homework.wallhaven.wh.WHGetThumbDimentions
import otus.gpb.homework.wallhaven.wh.WHStatus
import otus.gpb.homework.wallhaven.wh.WH_THUMB_MAX_DIMENTION
import java.lang.Math.random
import kotlin.random.Random


fun NavController.navigateToMain(navOptions: NavOptions) = navigate(MAIN_ROUTE, navOptions)

fun NavGraphBuilder.mainScreen() {
    composable(
        route = MAIN_ROUTE,
    ) {
        MainRoute()
    }
}
@Composable
internal fun MainRoute(
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel = hiltViewModel(),
) {
    MainScreen(
        data=viewModel.data(),
        state=viewModel.state(),
        settings = viewModel.settings(),
        modifier= Modifier
            .padding(all = 10.dp)
            //.scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
            //.verticalScroll(rememberScrollState())
    )
}

@Composable
internal fun MainScreen(
    settings: Settings,
    data: UiData,
    state:UiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier=modifier) {
        MainFilters(
            data=data,
            state=state,
            settings=settings,
        )
        Spacer(Modifier.height(16.dp))
        MainSorting(
            data=data,
            state=state,
            settings=settings,
            Modifier
                .align(Alignment.End)
        )
        Spacer(Modifier.height(16.dp))
        MainGrid(
            data=data,
            state=state,
            settings=settings,
        )
    }
}

@Preview
@Composable
private fun PreviewMainScreen() {
    AppTheme {
        MainScreen(
            modifier = Modifier,
            state=UiState(),
            settings = Settings(),
            data= UiData().apply { setContext(LocalContext.current) },
        )
    }
}

@Composable
internal fun MainFilters(
    data: UiData,
    state:UiState,
    settings: Settings,
    modifier: Modifier = Modifier,
) {
    Row(modifier=modifier) {
        Column(modifier = Modifier
            .weight(0.6f)
            .padding(all = 0.dp)
            .align(Alignment.Bottom)
        ) {
            OutlinedTextField(
                value = data.searchString.observeAsState().value!!,
                singleLine = true,
                onValueChange = {data.searchString.value=it },
                label = { Text(stringResource(R.string.main_search_by_tag)) },
            )
        }
        Column {
            ExtendedFloatingActionButton(
                onClick = {state.navigate(Navigation.FILTERS)},
                icon = { Icon(AppIcons.Filter,"") },
                text={ Text(stringResource(R.string.main_filters)) },
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                modifier= Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp, start = 8.dp)
            )
        }
    }
}

@Composable
internal fun MainSorting(
    data: UiData,
    state:UiState,
    settings: Settings,
    modifier: Modifier = Modifier,
) {
    val items=mapOf(
        WHSorting.DATE_ADDED to stringResource(R.string.main_sorting_date_added),
        WHSorting.RELEVANCE to stringResource(R.string.main_sorting_relevance),
        WHSorting.RANDOM to stringResource(R.string.main_sorting_random),
        WHSorting.VIEWS to stringResource(R.string.main_sorting_views),
        WHSorting.FAVORITES to stringResource(R.string.main_sorting_favorites),
        WHSorting.TOPLIST to stringResource(R.string.main_sorting_toplist),
    )
    Row(modifier=modifier) {
        Column(
            modifier=Modifier
                .width(160.dp)
        ){
            DropdownMenuBox(
                items = items,
                selected = settings.sorting.observeAsState().value!!,
                onSelect = { s -> settings.sorting.value = s },
                modifier = Modifier
                    .align(Alignment.End)
            )
        }
        Column(
            modifier=Modifier
                .padding(start= 8.dp)
        ) {
            FloatingActionButton(

                onClick = { settings.order.value = WHOrder.switch(settings.order.value!!) },
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
            ) {
                val icon = if (settings.order.observeAsState().value!! == WHOrder.DESC) {
                    AppIcons.SortDesc
                } else {
                    AppIcons.SortAsc
                }
                Icon(icon, "")
            }
       }
    }
}


@Composable
internal fun MainGrid(
    data: UiData,
    state:UiState,
    settings: Settings,
    modifier: Modifier = Modifier,
) {
    val total=data.imagesTotal.asIntState().intValue
    if (total==0) {
        Text("No data")
    } else {
        Text(total.toString())
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(WH_THUMB_MAX_DIMENTION.dp),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = {
                items(count = total) { idx ->
                    data.imagesData.collectAsState().value[idx]?.let {
                        when (it.thumbStatus.value) {
                            WHStatus.LOADED -> AsyncImage(
                                model = data.imageFromCache(it.id,WHFileType.THUMBNAIL),
                                contentScale = ContentScale.Crop,
                                contentDescription = null,
                                modifier = Modifier
                                    .width(it.thumbWidth.dp)
                                    .height(it.thumbHeight.dp)
                                )
                            else ->
                                Box(
                                    modifier = Modifier
                                        .width(it.thumbWidth.dp)
                                        .height(it.thumbHeight.dp)
                                        .clip(RectangleShape)
                                        .background(LocalGalleryColors.current.thumbNotLoaded)
                                )
                        }
                    } ?: run {
                        data.loadImageInfo(idx)
                        val seed = remember {
                            System.currentTimeMillis()
                        }
                        val resolutions = listOf(
                            Pair(1920, 1080),
                            Pair(1080, 1920),
                        )
                        val (iw, ih) = resolutions.random(Random(seed))
                        val (thumbWidth, thumbHeight) = WHGetThumbDimentions(iw, ih)
                        Box(
                            modifier = Modifier
                                .width(thumbWidth.dp)
                                .height(thumbHeight.dp)
                                .clip(RectangleShape)
                                .background(LocalGalleryColors.current.thumbUnknown)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}