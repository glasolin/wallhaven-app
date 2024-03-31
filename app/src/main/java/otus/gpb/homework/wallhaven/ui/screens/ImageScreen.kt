package otus.gpb.homework.wallhaven.ui.screens

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.asIntState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    ImageScreen(
        data = viewModel.data(),
        state = viewModel.state(),
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

    val idx=data.selectedImage.asIntState().intValue
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
            if (data.imagesData.contains(idx)) {
                ShowImageColors(
                    data = data,
                    state = state,
                    image =  data.imagesData[idx]!!,
                )
                if (data.imagesData[idx]!!.imageStatus == WHStatus.LOADED) {
                    Log.d(tag, "Recompose to ShowImage")
                    ShowImage(
                        data = data,
                        maxWidth = maxWidth,
                        image = data.imagesData[idx]!!,
                        modifier= Modifier
                            .padding(top = 10.dp)
                    )
                } else {
                    Log.d(tag, "Recompose to ShowImagePlaceholder")
                    ShowImagePlaceholder(maxWidth = maxWidth,image = data.imagesData[idx]!!)
                }
                if (data.imagesData[idx]!!.inFavorites) {
                    ExtendedFloatingActionButton(
                        onClick = { data.removeFromFavorites(data.imagesData[idx]!!) },
                        icon = { Icon(AppIcons.fromFavorites, "") },
                        text = { Text(stringResource(R.string.image_button_remove_from_favorites)) },
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 24.dp, bottom = 8.dp),
                    )
                } else {
                    ExtendedFloatingActionButton(
                        onClick = { data.addToFavorites(data.imagesData[idx]!!) },
                        icon = { Icon(AppIcons.toFavorites, "") },
                        text = { Text(stringResource(R.string.image_button_add_to_favorites)) },
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 24.dp, bottom = 8.dp),
                    )
                }
                if (data.imagesData[idx]!!.extendedInfoStatus == WHStatus.LOADED) {
                    ShowImageTags(
                        data = data,
                        state = state,
                        image = data.imagesData[idx]!!,
                    )
                }
            } else {
                ShowImageNoDataPlaceholder(maxWidth)
            }
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
            modifier = Modifier
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


@Composable
internal fun ShowImage(
    data:UiData,
    maxWidth:Int,
    image: ImageInfo,
    modifier: Modifier = Modifier,
) {
    val tag = "ShowImage"
    val painter =
        rememberAsyncImagePainter(model = File(data.imageFromCache(image.id, WHFileType.IMAGE)))
    val (iw,ih)=WHGetImageDimentions(image.width,image.height,maxWidth,1.0f/Resources.getSystem().displayMetrics.density)

    Log.d(tag,"Display is ${getScreenWidth()}x${getScreenHeight()} density is ${Resources.getSystem().displayMetrics.density}")
    Log.d(tag,"Image ${image.index} is (${iw}x${ih}) from (${image.width}x${image.height})")
    Image(
        painter = painter,
        contentDescription = "",
        contentScale = ContentScale.None,
        modifier = modifier
            .width(iw.dp)
            .height(ih.dp)
    )
}

@Composable
internal fun ShowImagePlaceholder(
    image:ImageInfo,
    maxWidth:Int,
    modifier: Modifier = Modifier,
) {
    val (iw,ih)=WHGetImageDimentions(image.width,image.height,maxWidth,1.0f/Resources.getSystem().displayMetrics.density)
    Box(
        modifier = Modifier
            .width(iw.dp)
            .height(iw.dp)
            .clip(RectangleShape)
            .background(LocalGalleryColors.current.imageNotLoaded)
    )
}

@Composable
internal fun ShowImageNoDataPlaceholder(
    maxWidth: Int,
    modifier: Modifier = Modifier,
) {
    val (iw,ih)=WHGetImageDimentions(1920,1980,maxWidth,1.0f/Resources.getSystem().displayMetrics.density)
    Box(
        modifier = Modifier
            .width(iw.dp)
            .height(ih.dp)
            .clip(RectangleShape)
            .background(LocalGalleryColors.current.imageUnknown)
    )
}