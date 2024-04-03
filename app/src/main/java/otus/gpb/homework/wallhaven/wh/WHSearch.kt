package otus.gpb.homework.wallhaven.wh

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.QueryMap

//https://wallhaven.cc/help/api

data class WHSearchRequest(
    var search: String?=null,
    var tags: List<String>?=null,
    var id: String?=null,
    var category:WHCategories?=null,
    var purity: WHPurity?=null,
    var order: WHOrder?=null,
    var sorting: WHSorting?=null,
    var color:WHColor?=null,
    var seed:String?=null,
    var ratio:WHRatio?=null,
    var page:Int?=null,
    var apiKey:String?=null,
    var width:Int?=null,
    var height:Int?=null,
    var image_id:String?=null,
) {
    fun queryMap() : Map<String,String> {
        val requestData =mutableMapOf<String,String>()
        val skip= mutableListOf<String>()

        val q = mutableListOf<String>()

        search?.let {
            if (it.isNotEmpty()) q.add(it.trim())
        }
        tags?.let {
            val t=if (it.isNotEmpty()) {
                it.joinToString(separator = " +", prefix = "+").trim()
            } else {
                ""
            }
            if (t.isNotEmpty()) {q.add(t)}
        }
        image_id?.let {
            if (it.isNotEmpty()) {
                q.add("like:$it")
            }
        }
        if (q.isNotEmpty()) {requestData["q"]=q.joinToString().trim()}

        if (!skip.contains("purity")) {
            when (purity) {
                WHPurity.SFW -> requestData["purity"] = "100"
                WHPurity.SKETCHY -> requestData["purity"] = "010"
                WHPurity.NSFW -> requestData["purity"] = "001"
                WHPurity.ALL -> requestData["purity"] = "111"
                null -> requestData["purity"] = "100"
            }
        }
        if (!skip.contains("category")) {
            when (category) {
                WHCategories.GENERAL -> requestData["categories"] = "100"
                WHCategories.ANIME -> requestData["categories"] = "010"
                WHCategories.PEOPLE -> requestData["categories"] = "001"
                WHCategories.ALL -> requestData["categories"] = "111"
                null -> requestData["categories"] = "111"
            }
        }

        if (!skip.contains("sorting")) {
            when (sorting) {
                WHSorting.DATE_ADDED -> requestData["sorting"] = "date_added"
                WHSorting.RELEVANCE -> requestData["sorting"] = "relevance"
                WHSorting.RANDOM -> requestData["sorting"] = "random"
                WHSorting.VIEWS -> requestData["sorting"] = "views"
                WHSorting.FAVORITES -> requestData["sorting"] = "favorites"
                WHSorting.TOPLIST -> requestData["sorting"] = "toplist"
                null -> {}
            }
        }

        if (!skip.contains("ratio")) {
            when (ratio) {
                WHRatio.R16x9 -> requestData["ratios"] = "16x9"
                WHRatio.R16x10 -> requestData["ratios"] = "16x10"
                WHRatio.R18x9 -> requestData["ratios"] = "18x9"
                WHRatio.R21x9 -> requestData["ratios"] = "21x9"
                WHRatio.R32x9 -> requestData["ratios"] = "32x9"
                WHRatio.R48x9 -> requestData["ratios"] = "48x9"
                WHRatio.R4x3 -> requestData["ratios"] = "4x3"
                WHRatio.R5x4 -> requestData["ratios"] = "5x4"
                WHRatio.R3x2 -> requestData["ratios"] = "3x2"
                WHRatio.R1x1 -> requestData["ratios"] = "1x1"
                WHRatio.R9x16 -> requestData["ratios"] = "9x16"
                WHRatio.R10x16 -> requestData["ratios"] = "10x16"
                WHRatio.R9x18 -> requestData["ratios"] = "9x18"
                WHRatio.R9x21 -> requestData["ratios"] = "9x21"
                WHRatio.R9x32 -> requestData["ratios"] = "9x32"
                WHRatio.R9x48 -> requestData["ratios"] = "9x48"
                WHRatio.R3x4 -> requestData["ratios"] = "3x4"
                WHRatio.R4x5 -> requestData["ratios"] = "4x5"
                WHRatio.R2x3 -> requestData["ratios"] = "2x3"
                WHRatio.ANY, null -> {}
            }
        }


        if (!skip.contains("order")) {
            when (order) {
                WHOrder.DESC -> requestData["order"] = "desc"
                WHOrder.ASC -> requestData["order"] = "asc"
                null -> {}
            }
        }

        if (seed?.isNotEmpty() == true) {requestData["seed"]=seed!!}

        if (page!=null && page!! >0 && page!! <9999) {requestData["page"]=page.toString()}

        if (!skip.contains("dimensions")) {
            if (width != null && height != null && width!! > 0 && width!! < 9999 && height!! > 0 && height!! < 9999) {
                requestData["resolutions"] = "${width}x${height}"
            }
        }
        if (!skip.contains("color")) {
            color?.let {
                if (it.name.isNotEmpty()) {
                    requestData["colors"] = it.name
                }
            }
        }

        if (apiKey?.isNotEmpty() == true) {requestData["apikey"]=apiKey!!}
        return  requestData
    }
}



data class WHSearchResponse(
    val data: List<Data>,
    val meta: Meta
) {
    data class Meta(
        val current_page: Int,
        val last_page: Int,
        val per_page: Int,
        val query: Any?,
        val seed: String?,
        val total: Int
    )
    data class Data(
        val category: String,
        val colors: List<String>,
        val created_at: String,
        val dimension_x: Int,
        val dimension_y: Int,
        val favorites: Int,
        val file_size: Int,
        val file_type: String,
        val id: String,
        val path: String,
        val purity: String,
        val ratio: String,
        val resolution: String,
        val short_url: String,
        val source: String,
        val thumbs: Thumbs,
        val url: String,
        val views: Int,
    )
    data class Thumbs(
        val large: String,
        val original: String,
        val small: String
    )
}

object RequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        Log.d("RequestInterceptor","Outgoing request to ${request.url}")
        return chain.proceed(request)
    }
}

interface WHSearchApi {
    @GET("search")
    suspend fun search(@QueryMap fields: Map<String,String>) : Response<WHSearchResponse>
}

class WHSearch {
    private fun getInstance(): Retrofit {
        /*
            q, categories, purity, sorting, order, topRange, atleast, resolutions, ratios, colors, page, seed
         */
        val okHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(RequestInterceptor)
            .build()
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(WH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    suspend fun search (request:WHSearchRequest):WHSearchResponse? {
        val retrofit = getInstance()
        val searchApi = retrofit.create(WHSearchApi::class.java)
        return try {
            val rc=searchApi.search(request.queryMap())
            if (rc.isSuccessful) {
                Log.d("WHSearch",rc.body().toString())
                rc.body() as WHSearchResponse
            } else {
                null
            }
        } catch (e:Exception) {
            null
        }
    }
}


