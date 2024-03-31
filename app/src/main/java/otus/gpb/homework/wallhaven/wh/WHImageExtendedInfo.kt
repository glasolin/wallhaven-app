package otus.gpb.homework.wallhaven.wh

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url
//https://wallhaven.cc/api/v1/w/<ID here>

data class WHExtendedImageInfoResponse(
    val data: Data
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
        val tags: List<Tag>,
        val thumbs: Thumbs,
        val uploader: Uploader,
        val url: String,
        val views: Int
    ) {
        data class Tag(
            val alias: String,
            val category: String,
            val category_id: Int,
            val created_at: String,
            val id: Int,
            val name: String,
            val purity: String
        )

        data class Thumbs(
            val large: String,
            val original: String,
            val small: String
        )

        data class Uploader(
            val avatar: Avatar,
            val group: String,
            val username: String
        ) {
            data class Avatar(
                val a128px: String,
                val a200px: String,
                val a20px: String,
                val a32px: String
            )
        }
    }
}
interface WHFetchImageExtendedInfo {
    @GET("w/{image_id}")
    suspend fun getInfo(@Path("image_id") id:String,@Query("apikey") apikey:String?) : Response<WHExtendedImageInfoResponse>
}

class WHImageExtendedInfo(
    private val apikey: String?
) {
    private val tag = "WHImageExtendedInfo"

    fun tagsFromExtendedInfo(info:WHExtendedImageInfoResponse?):List<WHTag> {
        val out = mutableListOf<WHTag>()
        info?.let {inf ->
            inf.data.tags.forEach() {
                out.add(WHTag(it.id, it.name))
            }
        }
        return out
    }

    suspend fun fetchTagsInfo(id:String):List<WHTag> {
        return tagsFromExtendedInfo(fetchExtendedInfo(id))
    }

    suspend fun fetchExtendedInfo(id: String): WHExtendedImageInfoResponse? {
        val okHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(RequestInterceptor)
            .build()
        val retrofit= Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(WH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val whApi = retrofit.create(WHFetchImageExtendedInfo::class.java)
        Log.d(tag,"retrofit builder completed for ${id}")
        var out: WHExtendedImageInfoResponse?=null
        try {
            val rc = whApi.getInfo(id,apikey)
            if (rc.isSuccessful) {
                Log.d(tag,"info fetched for ${id}")
                out=rc.body() as WHExtendedImageInfoResponse
            }
        } catch (e:Exception) {
            Log.d(tag,"exception $e");}
        return out
    }
}
