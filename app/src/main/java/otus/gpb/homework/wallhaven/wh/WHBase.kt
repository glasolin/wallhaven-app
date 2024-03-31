package otus.gpb.homework.wallhaven.wh

import android.graphics.Color
import androidx.core.graphics.toColor
import java.time.Instant

const val WH_BASE_URL = "https://wallhaven.cc/api/v1/"
const val WH_THUMB_MAX_DIMENTION=110

enum class WHLoadingStatus {
    NONE,LOADING,LOADED, FAILED
}

enum class WHStatus {
    NONE, INFO, LOADING, LOADED, ERROR
}
enum class WHSorting {
    DATE_ADDED, RELEVANCE, RANDOM, VIEWS, FAVORITES, TOPLIST
}

enum class WHCategories {
    GENERAL,ANIME,PEOPLE,ALL;
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

enum class WHPurity {
    SFW, SKETCHY, NSFW, ALL;
    companion object {
        fun fromString(s: String): WHPurity {
            return try {
                WHPurity.valueOf(s)
            } catch (e: IllegalArgumentException) {
                WHPurity.SFW
            }
        }
    }
}

enum class WHRatio {
    ANY, R16x9,R16x10,R18x9,R21x9,R32x9,R48x9,R4x3,R5x4,R3x2,R1x1,
         R9x16,R10x16,R9x18,R9x21,R9x32,R9x48,R3x4,R4x5,R2x3;
    companion object {
        fun fromString(s: String,width:Int=0,height: Int=0): WHRatio {
            return when (s) {

                "1.78" -> R16x9
                "1.6" -> R16x10
                "2" -> R18x9
                "2.33" -> R21x9
                "3.56" -> R32x9
                "5.33" -> R48x9
                "1.33" -> R4x3
                "1.25" -> R5x4
                "1.5" -> R3x2
                "1" -> R1x1
                "0.56" -> R9x16
                "0.63" -> R10x16
                "0.5" -> R9x18
                "0.43","0.45" -> R9x21
                "0.28" -> R9x32
                "0.19" -> R9x48
                "0.75" -> R3x4
                "0.8" -> R4x5
                "0.67","0.68" -> R2x3

                else -> ANY//throw IllegalArgumentException("Unknown ratio $s (${width}x${height})")
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

data class ImageInfo(
    var index:Int,
    val id: String,
    var thumbPath: String,
    val imagePath: String,
    val category: WHCategories,
    val colors: List<WHColor>,
    val purity: WHPurity,
    val ratio: WHRatio,
    val resolution: String,
    val source: String,
    val width: Int,
    val height: Int,
    val thumbWidth:Int,
    val thumbHeight:Int,
    val size: Int,
    val views: Int,
    var tags:List<WHTag>,
    var thumbStatus:WHStatus,
    var imageStatus:WHStatus,
    var extendedInfoStatus: WHStatus,
    var inFavorites: Boolean,
    var updated: Instant
)

data class WHTag(val id:Int,val tag:String)

data class WHColor(val name: String, val value: Color) {
    companion object {
        fun fromString(s: String):WHColor {
            val c:Color?=try {
                if ( s.trim().first().equals('#') ) {
                    Color.parseColor(s).toColor()
                } else {
                    Color.parseColor("#$s").toColor()
                }
            } catch (e:Exception) {
                null
                //Color.parseColor("#000000").toColor()
            }
            return if (c != null) {
                WHColor(colorToString(c),c)
            } else {
                WHColor("", Color.parseColor("#000000").toColor())
            }
        }

        private fun colorToString(c:Color):String {
            val rgb =  Color.rgb(c.red(),c.green(), c.blue())
            return String.format("%06X", 0xFFFFFF and rgb).lowercase()
        }
        fun toStringValue(v:WHColor):String {
            return colorToString(v.value)
        }

        fun toNamesList(list:List<WHColor>):List<String> {
            val colorString= mutableListOf<String>()
            list.forEach() {
                colorString.add(it.name.lowercase())
            }
            return colorString
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
    fun toNamesList():List<String> {
        var colorString= mutableListOf<String>()
        colors.forEach() {
            colorString.add(it.name)
        }
        return colorString
    }
}

fun emptyImage():ImageInfo {
    return ImageInfo(
        index = 0,
        category= WHCategories.GENERAL,
        colors= emptyList(),
        id  = "",
        imagePath = "",
        thumbPath = "",
        purity = WHPurity.SFW,
        ratio = WHRatio.ANY,
        resolution = "",
        source = "",
        views = 0,
        height = 0,
        width = 0,
        size = 0,
        thumbHeight = 0,
        thumbWidth = 0,
        tags = emptyList(),
        imageStatus = WHStatus.NONE,
        thumbStatus = WHStatus.NONE,
        extendedInfoStatus = WHStatus.NONE,
        inFavorites = false,
        updated = Instant.now()
    )
}

fun WHGetThumbDimentions(imageWidth:Int ,imageHeight:Int, multiplier:Float=1.0f, byWidth:Boolean=false):Pair<Int,Int> {
    if ((imageWidth==0) || (imageHeight==0)) {return Pair(0,0)}
    var thumbWidth=0
    var thumbHeight=0
    if ((imageWidth>imageHeight) || byWidth) {
        thumbWidth= (WH_THUMB_MAX_DIMENTION.times(multiplier)).toInt()
        thumbHeight=(thumbWidth*imageHeight).floorDiv(imageWidth)
    } else {
        thumbHeight= (WH_THUMB_MAX_DIMENTION.times(multiplier)).toInt()
        thumbWidth=(thumbHeight*imageWidth).floorDiv(imageHeight)
    }
    return Pair(thumbWidth,thumbHeight)
}


fun WHGetImageDimentions(imageWidth:Int, imageHeight:Int, maxWidth: Int, multiplier:Float=1.0f):Pair<Int,Int> {
    if ((imageWidth==0) || (imageHeight==0)) {return Pair(0,0)}
    var imgWidth=0
    var imgHeight=0
    imgWidth= (maxWidth.times(multiplier)).toInt()
    imgHeight=(imgWidth*imageHeight).floorDiv(imageWidth)
    return Pair(imgWidth,imgHeight)
}
