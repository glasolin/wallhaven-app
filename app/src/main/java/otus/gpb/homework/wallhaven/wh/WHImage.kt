package otus.gpb.homework.wallhaven.wh

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

enum class WHFileType {
    IMAGE,THUMBNAIL
}
interface WHFetchImage {
    @GET
    @Streaming
    suspend fun download(@Url file:String) : Response<ResponseBody>
}

class WHImage {
    private val tag = "WHImage"
    private val IMAGES_DIR ="images"
    private val THUMBS_DIR ="thumbs"
    private var cachePath: File? = null
        get() {requireNotNull(field){println("Cache path was not initialized")};return field}


    private fun getPath(type:WHFileType):String {
        return when (type) {
            WHFileType.IMAGE -> "$cachePath/$IMAGES_DIR"
            WHFileType.THUMBNAIL -> "$cachePath/$THUMBS_DIR"
        }
    }
    private fun checkCachedDirs() {
        val x = listOf(
            File(getPath(WHFileType.IMAGE)),
            File(getPath(WHFileType.THUMBNAIL))
        )
        x.forEach() { dir ->
            if (dir.exists() && !dir.isDirectory) {
                dir.delete()
            }
            if (!dir.exists()) {
                dir.mkdir()
            }
        }
    }

    fun setCachePath(path:File) {
        cachePath= path
        checkCachedDirs()
    }

    private fun getFileAbsPath(id:String, type:WHFileType):String {
        return "${getPath(type)}/$id"
    }
    private fun saveFile(body: ResponseBody?, id: String, type: WHFileType):String{
        if (body==null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            val path=getFileAbsPath(id,type)
            val fos = FileOutputStream(path)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            return path
        }catch (e:Exception){
            Log.e(tag,e.toString())
        }
        finally {
            input?.close()
        }
        return ""
    }
    suspend fun toCache(id:String, type:WHFileType, url:String):String {
        val okHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(RequestInterceptor)
            .build()
        val retrofit=Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val fileApi = retrofit.create(WHFetchImage::class.java)
        var out=""
        try {
            val rc = fileApi.download(url)
            if (rc.isSuccessful) {
                out=saveFile(rc.body(),id,type)
                Log.d(tag,rc.body().toString());
            }
        } catch (_:Exception) {}
        return out
    }
    fun inCache(id:String,type:WHFileType):Boolean {
        return File(getFileAbsPath(id,type)).exists()
    }

    fun fromCache(id:String,type:WHFileType): File {
        return File(getFileAbsPath(id,type))
    }

}