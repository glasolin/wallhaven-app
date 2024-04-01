package otus.gpb.homework.wallhaven.fav

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import otus.gpb.homework.wallhaven.SettingsData
import otus.gpb.homework.wallhaven.wh.ImageInfo
import otus.gpb.homework.wallhaven.wh.WHFileType
import otus.gpb.homework.wallhaven.wh.WHImage
import otus.gpb.homework.wallhaven.wh.checkDir
import otus.gpb.homework.wallhaven.wh.getDirSize
import java.io.File

@Entity
data class FavImages(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "json") val json: String,
)

@Dao
interface FavImagesDao {
    @Query("SELECT * FROM FavImages")
    suspend fun list(): List<FavImages>

    @Insert
    suspend fun add(vararg data: FavImages)

    @Query("DELETE FROM FavImages WHERE id=:id")
    suspend fun remove(id:String)

    @Query("DELETE FROM FavImages")
    suspend fun removeAll()

    @Query("SELECT * FROM FavImages WHERE id=:id LIMIT 1")
    suspend fun get(id:String):FavImages?
}

@Database(entities = [FavImages::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favImagesDao(): FavImagesDao
}



class Favorites(private val context: Context) {
    private val tag = "Favorites"
    private val IMAGES_DIR ="images"
    private val THUMBS_DIR ="thumbs"

    private var cachePath: File? = null
        get() {requireNotNull(field){println("Cache path was not initialized")};return field}

    private var dataPath: File? = null
        get() {requireNotNull(field){println("Data path was not initialized")};return field}


    private fun getDataPath(type: WHFileType):String {
        return when (type) {
            WHFileType.IMAGE -> "$dataPath/$IMAGES_DIR"
            WHFileType.THUMBNAIL -> "$dataPath/$THUMBS_DIR"
        }
    }

    private fun checkDataDirs() {
        if (cachePath?.isDirectory == true) {
            val x = listOf(
                File(getDataPath(WHFileType.IMAGE)),
                File(getDataPath(WHFileType.THUMBNAIL))
            )
            x.forEach() { dir ->
                checkDir(dir)
            }
        }
    }

     fun getDataFileAbsPath(id:String, type:WHFileType):String {
        return "${getDataPath(type)}/$id"
    }

    fun setPath(cacheDir:File,dataDir:File) {
        cachePath= cacheDir
        dataPath = dataDir
        checkDataDirs()
    }

    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java, "favorites"
    ).build()
    private val db_nots = Room.databaseBuilder(
        context,
        AppDatabase::class.java, "favorites"
    )
        .allowMainThreadQueries()
        .build()
    private val favDao = db.favImagesDao()

    suspend fun fetch():List<ImageInfo> {
        val list:List<FavImages> = favDao.list()
        val x=
            try {
                val x=list.map{
                    Json.decodeFromString<ImageInfo>(it.json)
                }
                x
            } catch (_:Exception) {
                emptyList()
            }
        return x
    }
    suspend fun add(img: ImageInfo):Boolean {
        var success=false
        if (!exists(img.id)) {
            if (copyToFavorites(img)) {
                favDao.add(FavImages(img.id, Json.encodeToString(img)))
                success=true
            }
        }
        return success
    }
    suspend fun remove(img: ImageInfo) {
        val fImage=File(getDataFileAbsPath(img.id,WHFileType.IMAGE))
        val fThumb=File(getDataFileAbsPath(img.id, WHFileType.IMAGE))
        try {
            fImage.delete()
            fThumb.delete()
        } catch (_:Exception) {}
        favDao.remove(img.id)
    }

    suspend fun removeAll() {
        try {
            dataPath!!.deleteRecursively()
            checkDataDirs()
        } catch (_:Exception) {}
        favDao.removeAll()
    }

    suspend fun exists(id:String):Boolean {
        val info=favDao.get(id)
        return info?.id?.isNotEmpty() ?: false
    }

    private fun copyToFavorites(img:ImageInfo):Boolean {
        val whi=WHImage().apply { setCachePath(cachePath!!) }
        return try {
            if (whi.inCache(img.id,WHFileType.THUMBNAIL)
                    && (whi.inCache(img.id,WHFileType.IMAGE))) {
                    val srcT=File(whi.getFileAbsPath(img.id,WHFileType.THUMBNAIL))
                    val trgT=File(getDataFileAbsPath(img.id, WHFileType.THUMBNAIL))
                    srcT.copyTo(trgT,true)
                    val srcI=File(whi.getFileAbsPath(img.id,WHFileType.IMAGE))
                    val trgI=File(getDataFileAbsPath(img.id, WHFileType.IMAGE))
                    srcT.copyTo(trgT,true)
                    true
            } else {
                false
            }
        } catch (_:Exception) {false}
    }

    fun fromData(id:String,type:WHFileType): File {
        return File(getDataFileAbsPath(id,type))
    }

    suspend fun nuke() {
        db_nots.clearAllTables()
    }
    fun getDataSize(): Long {
        return getDirSize(this.dataPath!!)
    }
}