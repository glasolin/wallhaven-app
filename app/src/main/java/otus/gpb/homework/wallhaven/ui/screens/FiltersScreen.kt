package otus.gpb.homework.wallhaven.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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
import otus.gpb.homework.wallhaven.ui.assets.DropdownMenuBox
import otus.gpb.homework.wallhaven.ui.navigation.FAVORITES_ROUTE
import otus.gpb.homework.wallhaven.ui.navigation.FILTERS_ROUTE
import otus.gpb.homework.wallhaven.ui.theme.AppIcons
import otus.gpb.homework.wallhaven.ui.theme.AppTheme
import otus.gpb.homework.wallhaven.ui.theme.Colors
import otus.gpb.homework.wallhaven.wh.WHCategories
import otus.gpb.homework.wallhaven.wh.WHColor
import otus.gpb.homework.wallhaven.wh.WHColors
import otus.gpb.homework.wallhaven.wh.WHPurity
import otus.gpb.homework.wallhaven.wh.WHRatio
import otus.gpb.homework.wallhaven.wh.WHSorting


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
    viewModel: MainActivityViewModel = hiltViewModel(),
) {
    FiltersScreen(
        data = viewModel.data(),
        modifier= Modifier
            .padding(all = 10.dp)
            //.fillMaxSize()
            //.scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
            //.verticalScroll(rememberScrollState())
    )
}
@Preview
@Composable
private fun PreviewFiltersScreen() {
    AppTheme {
        FiltersScreen(
            modifier = Modifier,
            data= UiData().apply { setContext(LocalContext.current) },
        )
    }
}

@Composable
internal fun FiltersScreen(
    data: UiData,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier= modifier
    ) {
        FiltersScreenCategoriesRow(
            data=data
        )
        FiltersScreenResolutionRow(
            data=data
        )
        FiltersScreenSectionTitle(text = stringResource(R.string.filters_colors_section))
        FiltersScreenColorsTable(
            data=data
        )
        FiltersScreenSectionTitle(text = stringResource(R.string.filters_tags_section))
        FiltersScreenTagsGrid(
            data=data
        )
    }
}

@Composable
private fun FiltersScreenSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp),
    )
}

@Composable
internal fun FiltersScreenCategoriesRow(
    data:UiData
) {
    val categories=mapOf(
        WHCategories.PEOPLE to stringResource(R.string.filters_categories_people),
        WHCategories.ANIME to stringResource(R.string.filters_categories_anime),
        WHCategories.GENERAL to stringResource(R.string.filters_categories_general),
        WHCategories.ALL to stringResource(R.string.filters_categories_all)
    )
    val purities=mapOf(
        WHPurity.SFW to stringResource(R.string.filters_purity_SFW),
        WHPurity.SKETCHY to stringResource(R.string.filters_purity_sketchy),
        WHPurity.NSFW to stringResource(R.string.filters_purity_NSFW),
        WHPurity.ALL to stringResource(R.string.filters_purity_all),
    )
    val ratios=mapOf(
        WHRatio.ANY to stringResource(R.string.filters_ratio_any),
        WHRatio.R16x9 to stringResource(R.string.filters_ratio_R16x9),
        WHRatio.R16x10 to stringResource(R.string.filters_ratio_R16x10),
        WHRatio.R18x9 to stringResource(R.string.filters_ratio_R18x9),
        WHRatio.R21x9 to stringResource(R.string.filters_ratio_R21x9),
        WHRatio.R32x9 to stringResource(R.string.filters_ratio_R32x9),
        WHRatio.R48x9 to stringResource(R.string.filters_ratio_R48x9),
        WHRatio.R4x3 to stringResource(R.string.filters_ratio_R4x3),
        WHRatio.R5x4 to stringResource(R.string.filters_ratio_R5x4),
        WHRatio.R3x2 to stringResource(R.string.filters_ratio_R3x2),
        WHRatio.R1x1 to stringResource(R.string.filters_ratio_R1x1),
        WHRatio.R9x16 to stringResource(R.string.filters_ratio_R9x16),
        WHRatio.R10x16 to stringResource(R.string.filters_ratio_R10x16),
        WHRatio.R9x18 to stringResource(R.string.filters_ratio_R9x18),
        WHRatio.R9x21 to stringResource(R.string.filters_ratio_R9x21),
        WHRatio.R9x32 to stringResource(R.string.filters_ratio_R9x32),
        WHRatio.R9x48 to stringResource(R.string.filters_ratio_R9x48),
        WHRatio.R3x4 to stringResource(R.string.filters_ratio_R3x4),
        WHRatio.R4x5 to stringResource(R.string.filters_ratio_R4x5),
        WHRatio.R2x3 to stringResource(R.string.filters_ratio_R2x3),
        WHRatio.R3x4 to stringResource(R.string.filters_ratio_R3x4),
    )
    Row() {
        Column(
            modifier = Modifier
                .weight(0.33f)
                .padding(4.dp)
        ) {
            DropdownMenuBox(
                items = categories,
                selected = data.settings().whCatehory.observeAsState().value!!,
                onSelect = { s -> data.settings().whCatehory.value = s },
                modifier = Modifier
            )
        }
        Column(
            modifier = Modifier
                .weight(0.33f)
                .padding(4.dp)
        ) {
            DropdownMenuBox(
                items = purities,
                selected = data.settings().whPurity.observeAsState().value!!,
                onSelect = { s -> data.settings().whPurity.value = s },
                modifier = Modifier
            )
        }
        Column(
            modifier = Modifier
                .weight(0.33f)
                .padding(4.dp)
        ) {
            DropdownMenuBox(
                items = ratios,
                selected = data.settings().whRatio.observeAsState().value!!,
                onSelect = { s -> data.settings().whRatio.value = s },
                modifier = Modifier
            )
        }
    }
}
@Composable
internal fun FiltersScreenResolutionRow(
    data:UiData
) {
    Row() {
        Column(
            modifier = Modifier
                .weight(0.35f)
                .padding(4.dp)
                .align(Alignment.CenterVertically)
        ){
            Text(
                style = MaterialTheme.typography.titleMedium,
                text=stringResource(R.string.filters_resolution)
            )
        }
        Column(
            modifier = Modifier
                .weight(0.30f)
                .padding(4.dp)
        ){
            val resolutionWidth = remember{ mutableStateOf(
                if (data.settings().whResolutionWidth.value!! > 0) {
                    data.settings().whResolutionWidth.value.toString()
                } else {""})
            }
            OutlinedTextField(
                value = resolutionWidth.value,
                singleLine = true,
                onValueChange = {
                    resolutionWidth.value=it
                    try {
                        data.settings().whResolutionWidth.value = it.toInt()
                        if (data.settings().whResolutionWidth.value!! <0 || data.settings().whResolutionWidth.value!! >9999) {
                            data.settings().whResolutionWidth.value=0
                        }
                    } catch (_:Exception) {data.settings().whResolutionWidth.value=0}
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.filters_resolution_width)) }
            )
        }
        Column(
            modifier = Modifier
                .weight(0.05f)
                .padding(4.dp)
                .align(Alignment.CenterVertically)
        ){
            Text(
                style = MaterialTheme.typography.titleMedium,
                text=stringResource(R.string.filters_resolution_separator),
            )
        }
        Column(
            modifier = Modifier
                .weight(0.30f)
                .padding(4.dp)
        ){
            val resolutionHeight = remember{ mutableStateOf(
                if (data.settings().whResolutionHeight.value!! > 0) {
                    data.settings().whResolutionHeight.value.toString()
                } else {""})
            }
            OutlinedTextField(
                value = resolutionHeight.value,
                singleLine = true,
                onValueChange = {
                    resolutionHeight.value=it
                    try {
                        data.settings().whResolutionHeight.value = it.toInt()
                        if (data.settings().whResolutionHeight.value!! <0 || data.settings().whResolutionHeight.value!! >9999) {
                            data.settings().whResolutionHeight.value=0
                        }
                    } catch (_:Exception) {data.settings().whResolutionHeight.value=0}
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.filters_resolution_height)) }
            )
        }
    }
}

@Composable
internal fun FiltersScreenColorsTable(
    data:UiData
) {
    val bWidth=30.dp
    val bHeight=40.dp
    val selectedColor = remember { mutableStateOf(
        data.settings().whColor.value!!
    )}
    LazyVerticalGrid(
        columns = GridCells.Fixed(5)
    ) {
        val clrs=WHColors.colors.toMutableList()
        data.settings().whColor.value?.let {
            if (!WHColors.colors.contains(WHColor.fromString(it))) {
                clrs.add(WHColor.fromString(it))
            }
        }
        items(clrs.size) {idx ->
            val c=Color(clrs[idx].value.red(),
                clrs[idx].value.green(),
                clrs[idx].value.blue())
            Box(
                modifier = Modifier
                    .width(bWidth)
                    .height(bHeight)
                    .clip(RectangleShape)
                    .padding(4.dp)
                    .background(color=c)
                    .clickable {
                        selectedColor.value=clrs[idx].name
                        data.settings().whColor.value=clrs[idx].name
                    }
            ) {
                if (clrs[idx].name == selectedColor.value) {
                    val whiteColors=listOf("ffffff","ffff00")
                    val iColor = if (whiteColors.contains(clrs[idx].name)) {
                        Colors.Black
                    } else {
                        Colors.White
                    }
                    Icon(
                        imageVector = AppIcons.Checked,
                        tint = iColor,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top=3.dp,start=22.dp)
                            .size(25.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun FiltersScreenTagsGrid(
    data:UiData
) {

}