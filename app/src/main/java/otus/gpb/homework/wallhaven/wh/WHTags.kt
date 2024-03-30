package otus.gpb.homework.wallhaven.wh

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

const val WH_TAGS_BASE_URL = "https://wallhaven.cc/"
data class WHTagsList(
    val results: List<Result>,
    val term: String
) {
    data class Result(
        val category: String,
        val filterBy: String,
        val id: Int,
        val label: String,
        val purity: String,
        val value: String
    )
}
interface WHTagsSuggestionApi {
    @GET("/autocomplete.php")
    suspend fun search(@Query("term") term:String) : Response<WHTagsList>
}

class WHTagsSuggestion {
    private val tag="WHTagsSuggestion"
    private fun getInstance(): Retrofit {
        val okHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(RequestInterceptor)
            .build()
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(WH_TAGS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    suspend fun suggest(term:String):WHTagsList? {
        val retrofit = getInstance()
        val suggestApi = retrofit.create(WHTagsSuggestionApi::class.java)
        try {
            val rc=suggestApi.search(term)
            if (rc!!.isSuccessful) {
                Log.d(tag,rc.body().toString());
                return rc.body() as WHTagsList
            } else {
                return null
            }
        } catch (e:Exception) {
            return null
        }
    }
}