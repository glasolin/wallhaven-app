package otus.gpb.homework.wallhaven.ui.screens

import android.content.res.Resources
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.asIntState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.ui.UiData
import otus.gpb.homework.wallhaven.ui.UiState
import otus.gpb.homework.wallhaven.ui.assets.DropdownMenuBox
import otus.gpb.homework.wallhaven.ui.navigation.MAIN_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.Navigation
import otus.gpb.homework.wallhaven.ui.theme.AppIcons
import otus.gpb.homework.wallhaven.ui.theme.AppTheme
import otus.gpb.homework.wallhaven.wh.WHOrder
import otus.gpb.homework.wallhaven.wh.WHSorting
import otus.gpb.homework.wallhaven.ui.theme.LocalGalleryColors
import otus.gpb.homework.wallhaven.wh.ImageInfo
import otus.gpb.homework.wallhaven.wh.WHFileType
import otus.gpb.homework.wallhaven.wh.WHGetThumbDimentions
import otus.gpb.homework.wallhaven.wh.WHPageStatus
import otus.gpb.homework.wallhaven.wh.WHStatus
import otus.gpb.homework.wallhaven.wh.WH_THUMB_MAX_DIMENTION
import java.io.File
import kotlin.random.Random


fun NavController.navigateToMain(navOptions: NavOptions) = navigate(MAIN_ROUTE, navOptions)

fun NavGraphBuilder.mainScreen() {
    composable(
        route = MAIN_ROUTE,
    ) {
        val viewModel: MainActivityViewModel = hiltViewModel()
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
        modifier= Modifier
            .padding(all = 10.dp)
            //.scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
            //.verticalScroll(rememberScrollState())
    )
}

@Composable
internal fun MainScreen(
    data: UiData,
    state:UiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier=modifier) {
        Row{
            MainSearch(
                data=data,
                state=state,
                modifier=Modifier
                    .weight(0.40f)
                )
             MainSortingList(
                data = data,
                state = state,
                modifier=Modifier
                    .padding(start=4.dp,top=8.dp)
                    .weight(0.45f)
            )
            MainOrder(
                data = data,
                state = state,
                modifier=Modifier
                    .padding(start=4.dp,top=8.dp)
                    .weight(0.15f)
            )
            MainFilter(
                data = data,
                state = state,
                modifier=Modifier
                    .padding(start=4.dp,top=8.dp)
                    .weight(0.150f)
            )
        }
        Spacer(Modifier.height(16.dp))
        MainGrid(
            data=data,
            state=state,
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
            data= UiData().apply { setContext(LocalContext.current) },
        )
    }
}


@Composable
internal fun MainFilter(
    data: UiData,
    state:UiState,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = {state.navigate(Navigation.FILTERS)},
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
            modifier= Modifier
                //.align(Alignment.End)
                //.padding(top = 8.dp, start = 8.dp)
        ) {
            Icon(AppIcons.Filter,"")
        }
    }
}

@Composable
internal fun MainSortingList(
    data: UiData,
    state:UiState,
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
    Column(
        modifier=modifier
    ){
        DropdownMenuBox(
            items = items,
            selected = data.settings().sorting.observeAsState().value!!,
            onSelect = { s -> data.settings().sorting.value = s },
            modifier = Modifier
                //.align(Alignment.End)
        )
    }
}

@Composable
internal fun MainSearch(
    data: UiData,
    state:UiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = data.searchString.observeAsState().value!!,
            singleLine = true,
            onValueChange = { data.searchString.value = it },
            label = { Text(stringResource(R.string.main_search_by_tag)) },
        )
    }
}

@Composable
internal fun MainOrder(
    data: UiData,
    state:UiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier=modifier
    ) {
        FloatingActionButton(
            onClick = { data.settings().order.value = WHOrder.switch(data.settings().order.value!!) },
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
        ) {
            val icon = if (data.settings().order.observeAsState().value!! == WHOrder.DESC) {
                AppIcons.SortDesc
            } else {
                AppIcons.SortAsc
            }
            Icon(icon, "")
        }
    }
}

@Composable
internal fun MainGrid(
    data: UiData,
    state:UiState,
    modifier: Modifier = Modifier,
) {
    val tag="FavoritesMainGrid"
    val total=data.imagesTotal.intValue
    when (data.imagesTotal.intValue) {
        0 -> {
            Text("No data")
        }
        -1 -> {
            data.loadImageInfo(0)
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
                            data.loadImageInfo(idx)
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

@Composable
internal fun ShowThumbnail(
    data:UiData,
    state: UiState,
    image:ImageInfo,
    modifier: Modifier = Modifier,
) {

    val painter =
        rememberAsyncImagePainter(model = File(data.imageFromCache(image.id, WHFileType.THUMBNAIL)))

    Image(
        painter = painter,
        contentDescription = "",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .width(image.thumbWidth.dp)
            .height(image.thumbHeight.dp)
            .clickable(true, null) {
                data.selectImage(image.index)
                state.navigate(Navigation.IMAGE)
            }
    )
}

@Composable
internal fun ShowThumbnailPlaceholder(
    image:ImageInfo,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .width(image.thumbWidth.dp)
            .height(image.thumbHeight.dp)
            .clip(RectangleShape)
            .background(LocalGalleryColors.current.thumbNotLoaded)
        )
}

@Composable
internal fun ShowGalleryPlaceholder() {
    val seed = remember {
        System.currentTimeMillis()
    }
    val resolutions = listOf(
        Pair(1920, 1080),
        Pair(1080, 1920),
    )
    val (iw, ih) = resolutions.random(Random(seed))
    val (thumbWidth, thumbHeight) = WHGetThumbDimentions(iw, ih,0.5f)
    Box(
        modifier = Modifier
            .width(thumbWidth.dp)
            .height(thumbHeight.dp)
            .clip(RectangleShape)
            .background(LocalGalleryColors.current.thumbUnknown)
    )
}