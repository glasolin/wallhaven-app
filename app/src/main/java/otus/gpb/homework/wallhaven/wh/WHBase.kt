package otus.gpb.homework.wallhaven.wh

import android.graphics.Color
import androidx.core.graphics.toColor
import androidx.core.graphics.toColorInt

const val WH_BASE_URL = "https://wallhaven.cc/api/v1/"

enum class WHSorting {
    DATE_ADDED, RELEVANCE, RANDOM, VIEWS, FAVORITES, TOPLIST
}

enum class WHCategories {
    GENERAL,ANIME,PEOPLE
}

enum class WHPurity {
    SFW, SKETCHY, NSFW
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

data class WHColor(val name: String, val value: Color)

object WHColors {
    val colors:List<WHColor> = listOf("660000","990000","cc0000","cc3333","ea4c88","993399","663399",
        "333399","0066cc","0099cc","66cccc","77cc33","669900","336600",
        "666600","999900","cccc33","ffff00","ffcc33","ff9900","ff6600",
        "cc6633","996633","663300","000000","999999","cccccc","ffffff",
        "424153")
        .map{
            WHColor("#$it",Color.parseColor("#$it").toColor())
        }

}
