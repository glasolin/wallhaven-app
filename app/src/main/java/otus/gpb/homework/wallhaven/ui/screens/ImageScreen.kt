package otus.gpb.homework.wallhaven.ui.screens

import android.content.res.Resources
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import coil.compose.rememberAsyncImagePainter
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.getScreenHeight
import otus.gpb.homework.wallhaven.getScreenWidth
import otus.gpb.homework.wallhaven.ui.ImageMode
import otus.gpb.homework.wallhaven.ui.UiData
import otus.gpb.homework.wallhaven.ui.UiState
import otus.gpb.homework.wallhaven.ui.assets.FlowRow
import otus.gpb.homework.wallhaven.ui.assets.NonlazyGrid
import otus.gpb.homework.wallhaven.ui.navigation.IMAGE_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.Navigation
import otus.gpb.homework.wallhaven.ui.theme.AppIcons
import otus.gpb.homework.wallhaven.ui.theme.AppTheme
import otus.gpb.homework.wallhaven.ui.theme.LocalGalleryColors
import otus.gpb.homework.wallhaven.wh.ImageInfo
import otus.gpb.homework.wallhaven.wh.WHFileType
import otus.gpb.homework.wallhaven.wh.WHGetImageDimentions
import otus.gpb.homework.wallhaven.wh.WHStatus
import java.io.File


fun NavController.navigateToImage(navOptions: NavOptions) = navigate(IMAGE_ROUTE, navOptions)

fun NavGraphBuilder.imageScreen() {
    composable(
        route = IMAGE_ROUTE,
    ) {
        ImageRoute()
    }
}
@Composable
internal fun ImageRoute(
    viewModel: MainActivityViewModel = hiltViewModel(),
) {
    val data = viewModel.data()
    val state = viewModel.state()
    data.selectedImage?.value?.let {
        state.setDynamicTitle("${it.width}x${it.height}, ${data.bytesToHuman(it.size.toLong())}")
    }
    ImageScreen(
        data = data,
        state = state,
        modifier= Modifier
            .padding(all = 10.dp)
            .scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
            .verticalScroll(rememberScrollState())
    )
}
@Preview
@Composable
private fun PreviewImageScreen() {
    AppTheme {
        ImageScreen(
            modifier = Modifier,
            state = UiState(),
            data= UiData().apply { setContext(LocalContext.current) },
        )
    }
}

@Composable
internal fun ImageScreen(
    data: UiData,
    state: UiState,
    modifier: Modifier = Modifier,
) {
    val tag = "ImageScreen"

    val columnSize = remember { mutableStateOf(Size.Zero) }
    Log.d(tag,"Recompose ImageScreen")
    Row (
        modifier=modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { layoutCoordinates ->
                    columnSize.value = layoutCoordinates.size.toSize()
                }
        ) {
            val maxWidth = LocalDensity.current.run {
                (columnSize.value.width).toInt()
            }
            data.selectedImage.value?.let { img ->
                Row {
                    Column(
                        modifier = Modifier
                            .weight(0.05f)
                            .padding(top = 4.dp)
                    ) {
                        data.previousImage.value?.let {
                            Icon(
                                AppIcons.imageToLeft,
                                "",
                                Modifier.clickable { data.toPreviousImage() }
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                    ) {
                        ShowImageColors(
                            data = data,
                            state = state,
                            image = data.selectedImage.value!!,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.05f)
                            .padding(top = 4.dp)
                    ) {
                        data.nextImage.value?.let {
                            Icon(
                                AppIcons.imageToRight, "",Modifier.clickable { data.toNextImage() }
                            )
                        }
                    }
                }
                Column () {
                    if (img.imageStatus == WHStatus.LOADED) {
                        Log.d(tag, "Recompose to ShowImage")
                        ShowImage(
                            data = data,
                            state = state,
                            maxWidth = maxWidth,
                            image = img,
                            modifier = Modifier
                                .padding(top = 10.dp)
                        )
                        Row (
                            modifier=Modifier
                                .padding(top = 24.dp, bottom = 8.dp),
                        ){
                            Column(
                                modifier = Modifier
                                    .weight(0.3f)
                                    .padding(top = 16.dp)
                            ) {
                                Row {
                                    Icon(
                                        AppIcons.imageViews, "",
                                    )
                                    Text(img.views.toString())
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .weight(0.7f)
                            ) {
                                if (img.inFavorites) {
                                    ExtendedFloatingActionButton(
                                        onClick = { data.removeFromFavorites(img) },
                                        icon = { Icon(AppIcons.fromFavorites, "") },
                                        text = { Text(stringResource(R.string.image_button_remove_from_favorites)) },
                                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                                        modifier = Modifier
                                            .align(Alignment.End)
                                    )
                                } else {
                                    ExtendedFloatingActionButton(
                                        onClick = { data.addToFavorites(img) },
                                        icon = { Icon(AppIcons.toFavorites, "") },
                                        text = { Text(stringResource(R.string.image_button_add_to_favorites)) },
                                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                                        modifier = Modifier
                                            .align(Alignment.End)
                                    )
                                }
                            }
                            Column(
                                modifier=Modifier
                                    .padding(start=8.dp)
                            ) {
                                FloatingActionButton(
                                    onClick = {state.share(File(data.imageFromCache(img.id,WHFileType.IMAGE)))}
                                ) {
                                    Icon(AppIcons.Share,"")
                                }
                            }
                        }
                    } else {
                        Log.d(tag, "Recompose to ShowImagePlaceholder")
                        ShowImagePlaceholder(
                            maxWidth = maxWidth,
                            image = img,
                            data = data,
                            modifier = Modifier
                                .padding(top = 10.dp),
                        )
                    }
                }

                if (img.extendedInfoStatus == WHStatus.LOADED) {
                    ShowImageTags(
                        data = data,
                        state = state,
                        image = img,
                    )
                }
            } ?: ShowImageNoDataPlaceholder(maxWidth)
        }
    }
}


@Composable
internal fun ShowImageColors(
    data:UiData,
    state:UiState,
    image: ImageInfo,
    modifier: Modifier = Modifier,
) {
    val bWidth=100.dp
    val bHeight=30.dp

    NonlazyGrid(
        columns = 5,
        itemCount = image.colors.size,
        modifier = Modifier
            .padding(start = 7.5.dp, end = 7.5.dp)
    ) { idx ->
        val c= Color(image.colors[idx].value.red(),
            image.colors[idx].value.green(),
            image.colors[idx].value.blue())
        Box(
            modifier = modifier
                .width(bWidth)
                .height(bHeight)
                .clip(RectangleShape)
                .padding(4.dp)
                .background(color = c)
                .clickable {
                    data.loadFromColor(image.colors[idx])
                    state.navigate(Navigation.MAIN)
                }
        )
    }
}
@Composable
internal fun ShowImageTags(
    data:UiData,
    state: UiState,
    image: ImageInfo,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        verticalGap = 1.dp,
        horizontalGap = 1.dp,
        alignment = Alignment.CenterHorizontally
    ) {
        image.tags.forEach() {
            TextButton(
                onClick = {
                    data.loadFromTag(it)
                    state.navigate(Navigation.MAIN)
                }
            ) {
                Text("#${it.tag}")
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ShowImage(
    data:UiData,
    state:UiState,
    maxWidth:Int,
    image: ImageInfo,
    modifier: Modifier = Modifier,
) {
    val tag = "ShowImage"
    val painter =
        rememberAsyncImagePainter(model = File(
            if (image.inFavorites) {
                data.imageFromFavorite(image.id,WHFileType.IMAGE)
            } else {
                data.imageFromCache(image.id, WHFileType.IMAGE)
            }
        ))
    val (iw,ih)=WHGetImageDimentions(image.width,image.height,maxWidth,1.0f/Resources.getSystem().displayMetrics.density)

    Log.d(tag,"Display is ${getScreenWidth()}x${getScreenHeight()} density is ${Resources.getSystem().displayMetrics.density}")
    Log.d(tag,"Image ${image.index} is (${iw}x${ih}) from (${image.width}x${image.height}, thumb ${image.thumbWidth}x${image.thumbHeight}), maxWidth is $maxWidth")
    val scale=if (iw>ih) {
        ContentScale.FillWidth
    } else {
        ContentScale.FillHeight
    }
    Image(
        painter = painter,
        contentDescription = "",
        contentScale = scale,
        modifier = modifier
            .width(iw.dp)
            .height(ih.dp)
            .clickable(onClick = {
                data.loadFromImage(image)
                state.navigate(Navigation.MAIN)
            })
    )
    /*val items=listOf(data.previousImage,data.selectedImage,data.nextImage)
    val pagerState = rememberPagerState(pageCount = {
        items.size
    })
    LaunchedEffect(pagerState) {
        // Collect from the a snapshotFlow reading the currentPage
        snapshotFlow { pagerState.currentPage }.collect { index ->
            items[index].value?.let { img ->
                when (data.imageMode) {
                    ImageMode.SEARCH -> data.selectImage(img.index)
                    ImageMode.FAVORITES -> data.selectFavouriteImage(img)
                }
            }
        }
    }

    HorizontalPager(
        state = pagerState,
    ) { index ->
        items[index].value?.let { img ->
            val painter =
                rememberAsyncImagePainter(model = File(
                    if (img.inFavorites) {
                        data.imageFromFavorite(img.id,WHFileType.IMAGE)
                    } else {
                        data.imageFromCache(img.id, WHFileType.IMAGE)
                    }
                ))
            val (iw,ih)=WHGetImageDimentions(img.width,img.height,maxWidth,1.0f/Resources.getSystem().displayMetrics.density)
            val scale=if (iw>ih) {
                ContentScale.FillWidth
            } else {
                ContentScale.FillHeight
            }
            Image(
                painter = painter,
                contentDescription = "",
                contentScale = scale,
                modifier = modifier
                    .width(iw.dp)
                    .height(ih.dp)
                    .clickable(onClick = {
                        data.loadFromImage(img)
                        state.navigate(Navigation.MAIN)
                    })
            )
        }
    }*/
}
/* TODO
  Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        pageCount = animals.size,
                        state = pagerState,
                        key = { animals[it] },
                        pageSize = PageSize.Fill
                    ) { index ->
                        Image(
                            painter = painterResource(id = animals[index]),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .offset(y = -(16).dp)
                            .fillMaxWidth(0.5f)
                            .clip(RoundedCornerShape(100))
                            .background(MaterialTheme.colors.background)
                            .padding(8.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        pagerState.currentPage - 1
                                    )
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Go back"
                            )
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        pagerState.currentPage + 1
                                    )
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Go forward"
                            )
                        }
                    }
                }
            }
        }
 */

@Composable
internal fun ShowImagePlaceholder(
    data:UiData,
    image:ImageInfo,
    maxWidth:Int,
    modifier: Modifier = Modifier,
) {
    val tag = "ShowImagePlaceholder"
    val (iw,ih)=WHGetImageDimentions(image.thumbWidth,image.thumbHeight,maxWidth,1.0f/Resources.getSystem().displayMetrics.density)
    Log.d(tag,"Image ${image.index} is (${iw}x${ih}) from (${image.width}x${image.height}, thumb ${image.thumbWidth}x${image.thumbHeight}), maxWidth is $maxWidth")
    if (!image.inFavorites && image.thumbStatus==WHStatus.LOADED) {
        val painter =
            rememberAsyncImagePainter(model = File(data.imageFromCache(image.id, WHFileType.THUMBNAIL)))
        val scale=if (iw>ih) {
            ContentScale.FillWidth
        } else {
            ContentScale.FillHeight
        }
        Image(
            painter = painter,
            contentDescription = "",
            contentScale = scale,
            modifier = modifier
                .width(iw.dp)
                .height(ih.dp)
                //.background(Color.Yellow)
        )
    } else {
        Box(
            modifier = Modifier
                .width(iw.dp)
                .height(iw.dp)
                .clip(RectangleShape)
                .background(LocalGalleryColors.current.imageNotLoaded)
        )
    }
}

@Composable
internal fun ShowImageNoDataPlaceholder(
    maxWidth: Int,
    modifier: Modifier = Modifier,
) {
    val (iw,ih)=WHGetImageDimentions(1920,1980,maxWidth,1.0f/Resources.getSystem().displayMetrics.density)
    Box(
        modifier = modifier
            .width(iw.dp)
            .height(ih.dp)
            .clip(RectangleShape)
            .background(LocalGalleryColors.current.imageUnknown)
    )
}