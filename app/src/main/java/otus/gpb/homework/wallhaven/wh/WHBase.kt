package otus.gpb.homework.wallhaven.wh

import android.graphics.Color
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.toColor
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val WH_BASE_URL = "https://wallhaven.cc/api/v1/"
const val WH_THUMB_MAX_DIMENTION=150

enum class WHLoadingStatus {
    NONE,LOADING,LOADED
}
enum class WHSorting {
    DATE_ADDED, RELEVANCE, RANDOM, VIEWS, FAVORITES, TOPLIST
}

enum class WHCategories {
    GENERAL,ANIME,PEOPLE;
    companion object {
        fun fromString(s: String): WHCategories {
            return try {
                valueOf(s)
            } catch (e: IllegalArgumentException) {
                GENERAL
            }
        }
    }
}

enum class WHStatus {
    NONE, INFO, LOADING, LOADED
}

enum class WHPurity {
    SFW, SKETCHY, NSFW;
    companion object {
        fun fromString(s: String): WHPurity {
            return try {
                WHPurity.valueOf(s)
            } catch (e: IllegalArgumentException) {
                WHPurity.NSFW
            }
        }
    }
}

enum class WHOrder {
    DESC, ASC;
    companion object {
        fun switch(v: WHOrder): WHOrder {
            return if (v == DESC) {
                ASC
            } else {
                DESC
            }
        }
    }
}

data class Image(
    val id: String,
    val thumbPath: String,
    val imagePath: String,
    val category: WHCategories,
    val colors: List<WHColor>,
    val purity: WHPurity,
    val ratio: String,
    val resolution: String,
    val source: String,
    val width: Int,
    val height: Int,
    val thumbWidth:Int,
    val thumbHeight:Int,
    val size: Int,
    val views: Int,
    var thumbStatus:MutableStateFlow<WHStatus>,
    var imageStatus:MutableStateFlow<WHStatus>,
)

data class WHColor(val name: String, val value: Color) {
    companion object {
        fun fromString(s: String):WHColor {
            val c:Color=try {
                if ( s.trim().first().equals("#") ) {
                    Color.parseColor(s).toColor()
                } else {
                    Color.parseColor("#$s").toColor()
                }
            } catch (e:Exception) {
                Color.parseColor("#000000").toColor()
            }
            return WHColor(c.toString(),c)
        }
    }
}

object WHColors {
    val colors:List<WHColor> = listOf("660000","990000","cc0000","cc3333","ea4c88","993399","663399",
        "333399","0066cc","0099cc","66cccc","77cc33","669900","336600",
        "666600","999900","cccc33","ffff00","ffcc33","ff9900","ff6600",
        "cc6633","996633","663300","000000","999999","cccccc","ffffff",
        "424153")
        .map{
            WHColor.fromString(it)
        }
}

fun emptyImage():Image {
    return Image(
        category= WHCategories.GENERAL,
        colors= emptyList(),
        id  = "",
        imagePath = "",
        thumbPath = "",
        purity = WHPurity.NSFW,
        ratio = "",
        resolution = "",
        source = "",
        views = 0,
        height = 0,
        width = 0,
        size = 0,
        thumbHeight = 0,
        thumbWidth = 0,
        imageStatus = MutableStateFlow<WHStatus>(WHStatus.NONE),
        thumbStatus = MutableStateFlow<WHStatus>(WHStatus.NONE),
    )
}

fun WHGetThumbDimentions(imageWidth:Int ,imageHeight:Int):Pair<Int,Int> {
    if ((imageWidth==0) || (imageHeight==0)) {return Pair(0,0)}
    var thumbWidth=0
    var thumbHeight=0
    if (imageWidth>imageHeight) {
        thumbWidth= WH_THUMB_MAX_DIMENTION
        thumbHeight=(thumbWidth*imageHeight).floorDiv(imageWidth)
    } else {
        thumbHeight= WH_THUMB_MAX_DIMENTION
        thumbWidth=(thumbHeight*imageWidth).floorDiv(imageHeight)
    }
    return Pair(thumbWidth,thumbHeight)
}
