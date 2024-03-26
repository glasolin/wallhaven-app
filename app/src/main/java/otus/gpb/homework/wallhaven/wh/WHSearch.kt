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
    var colors:List<WHColor>?=null
)



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
        Log.d("RequestInterceptor","Outgoing request to ${request.url()}")
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
        var requestData =mutableMapOf<String,String>()
        /*if (request.search != null) {requestData["q"]= request.search!!
        } else {requestData["q"]=""}*/

        requestData["q"]=if (request.search!=null) {request.search!!} else {""}

        val tags=request.tags?.joinToString(separator = " ","+tag=") { it }
        if (tags != null)  requestData["q"] = String().concat

        val rc=searchApi.search(requestData)
        try {
            if (rc!!.isSuccessful) {return rc.body() as WHSearchResponse} else {return null}
        } catch (e:Exception) {
            return null
        }
    }
}


