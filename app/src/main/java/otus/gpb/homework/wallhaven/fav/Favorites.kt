package otus.gpb.homework.wallhaven.fav

import android.content.Context
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

@Entity
data class FavImages(
    @PrimaryKey val id: String
)

@Dao
interface FavImagesDao {
    @Query("SELECT * FROM FavImages")
    suspend fun list(): List<FavImages>

    @Insert
    suspend fun add(vararg data: FavImages)

    @Delete
    suspend fun remove(data: FavImages)

    @Query("DELETE FROM FavImages")
    suspend fun removeAll()

    @Query("SELECT * FROM FavImages WHERE id=:id LIMIT 1")
    suspend fun get(id:String):FavImages
}

@Database(entities = [FavImages::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favImagesDao(): FavImagesDao
}



class Favorites(private val context: Context) {
    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java, "favorites"
    ).build()
    private val favDao = db.favImagesDao()

    suspend fun fetch():List<String> {
        val list:List<FavImages> = favDao.list()
        return list.map{it.id}
    }
    suspend fun add(id:String) {
        if (!exists(id)) {
            favDao.add(FavImages(id))
        }
    }
    suspend fun remove(id:String) {
        favDao.remove(FavImages(id))
    }

    suspend fun removeAll() {
        favDao.removeAll()
    }
    suspend fun exists(id:String):Boolean {
        val info=favDao.get(id)
        if (info == null) {
            return false
        } else {
            return info.id.isNotEmpty()
        }
    }
}