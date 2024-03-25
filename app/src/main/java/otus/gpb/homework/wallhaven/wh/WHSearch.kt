package otus.gpb.homework.wallhaven.wh

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.FieldMap

//https://wallhaven.cc/help/api

data class WHSearchRequest(
    val tags: List<String>?,
    val id: String?,
    val categories:WHCategories?,
    val purity: WHPurity?,
    val order: WHOrder?,
    val colors:List<WHColor>?
)

data class WHSearchResponse(
    val data: List<Data>,
    val meta: Meta
) {
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
        val views: Int
    ) {
        data class Thumbs(
            val large: String,
            val original: String,
            val small: String
        )
    }

    data class Meta(
        val current_page: Int,
        val last_page: Int,
        val per_page: Int,
        val query: Any?,
        val seed: Any?,
        val total: Int
    )
}

object RequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        println("Outgoing request to ${request.url()}")
        return chain.proceed(request)
    }
}

interface WHSearchApi {
    @GET("search")
    suspend fun search(@FieldMap fields: Map<String,String>) : Response<WHSearchResponse>
}

class HWSearch {
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
    suspend fun search(){
        val retrofit = getInstance()
        val searchApi = retrofit.create(WHSearchApi::class.java)
        val data=mapOf(Pair("q","xxxxxxx"))
        val rc=searchApi.search(data)
    }
}


