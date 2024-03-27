package otus.gpb.homework.wallhaven.wh

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.QueryMap

//https://wallhaven.cc/help/api

data class WHSearchRequest(
    var search: String?=null,
    var tags: List<String>?=null,
    var id: String?=null,
    var categories:WHCategories?=null,
    var purity: WHPurity?=null,
    var order: WHOrder?=null,
    var sorting: WHSorting?=null,
    var colors:List<WHColor>?=null,
    var seed:String?=null,
    var page:Int?=null,
) {
    fun queryMap() : Map<String,String> {
        val requestData =mutableMapOf<String,String>()

        val q= mutableListOf<String>()

        if (search!=null) {q.add(search!!)}

        val tags=tags?.joinToString(separator = " ","+tag=") { it }
        if (tags != null) {q.add(tags)}

        if (q.isNotEmpty()) {requestData["q"]=q.joinToString { " " }}

        when (purity) {
            WHPurity.SFW -> requestData["purity"]="100"
            WHPurity.SKETCHY -> requestData["purity"]="010"
            WHPurity.NSFW -> requestData["purity"]="001"
            null -> requestData["purity"]="111"
        }
        when (categories) {
            WHCategories.GENERAL -> requestData["category"]="100"
            WHCategories.ANIME -> requestData["category"]="010"
            WHCategories.PEOPLE -> requestData["category"]="001"
            null -> requestData["category"]="111"
        }

        when (sorting) {
            WHSorting.DATE_ADDED -> requestData["sorting"]="date_added"
            WHSorting.RELEVANCE ->  requestData["sorting"]="relevance"
            WHSorting.RANDOM -> requestData["sorting"]="random"
            WHSorting.VIEWS -> requestData["sorting"]="views"
            WHSorting.FAVORITES -> requestData["sorting"]="favorites"
            WHSorting.TOPLIST -> requestData["sorting"]="toplist"
            null -> {}
        }

        when (order) {
            WHOrder.DESC -> requestData["order"]="desc"
            WHOrder.ASC -> requestData["order"]="asc"
            null -> {}
        }

        if (seed?.isNotEmpty() == true) {requestData["seed"]=seed!!}

        if (page!=null && page!! >0 && page!! <9999) {requestData["page"]=page.toString()}

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
        val seed: Any?,
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
        try {
            val rc=searchApi.search(request.queryMap())
            if (rc!!.isSuccessful) {Log.d("WHSearch",rc.body().toString());return rc.body() as WHSearchResponse} else {return null}
        } catch (e:Exception) {
            return null
        }
    }
}


