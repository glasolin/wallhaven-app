package otus.gpb.homework.wallhaven.ui.assets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun <T> PieChart(
    modifier: Modifier = Modifier,
    data: Map<T,Triple<String, Float, Color>>,
    animated: Boolean = true,
    selected: T,
    onSelect: (selectedItem: T) -> Unit,
) {
    val chartDegrees = 360f // circle shape
    var startAngle = 270f // top to right

    val colors = mutableListOf<Color>()
    val names = mutableListOf<String>()
    val values= mutableListOf<Float>()

    data.forEach() { (id,item) ->
        val (name,value,color)=item
        colors.add(color)
        names.add(name)
        values.add(value)
    }

    var total=values.sum()
    val proportions = values.map {v ->
        v * 100 / total
    }

    // calculate each input slice degrees
    val angleProgress = proportions.map {v ->
        chartDegrees * v / 100
    }

    // calculate each slice end point in degrees, for handling click position
    val progressSize = mutableListOf<Float>()

    LaunchedEffect(angleProgress){
        progressSize.add(angleProgress.first())
        for (x in 1 until angleProgress.size) {
            progressSize.add(angleProgress[x] + progressSize[x - 1])
        }
    }
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {

        val canvasSize = min(constraints.maxWidth, constraints.maxHeight)
        val size = Size(canvasSize.toFloat(), canvasSize.toFloat())
        val canvasSizeDp = with(LocalDensity.current) { canvasSize.toDp() }

        Canvas(modifier = Modifier.size(canvasSizeDp)) {

            angleProgress.forEachIndexed { index, angle ->
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = angle,
                    useCenter = true,
                    size = size,
                    style = Fill
                )
                startAngle += angle
            }
        }
    }
}

fun Int.textDp(density: Density): TextUnit = with(density) {
    this@textDp.dp.toSp()
}

val Int.textDp: TextUnit
    @Composable get() =  this.textDp(density = LocalDensity.current)