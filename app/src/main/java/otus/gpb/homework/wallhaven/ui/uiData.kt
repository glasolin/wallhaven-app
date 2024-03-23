package otus.gpb.homework.wallhaven.ui

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.getString
import otus.gpb.homework.wallhaven.R
import java.io.File

enum class storeDataTypes {
    NONE,FAVORITES, CACHE, FREE
}

class UiData {
    private var context: Context?=null
        get() {requireNotNull(field){println("Context was not initialized")};return field}

    var storeUsage = mutableStateOf<Map<storeDataTypes,Long>>((emptyMap ()))

    init {
        updateStorageUsage()
    }
    fun setContext(context: Context) {
        this.context=context
    }

    private fun updateStorageUsage() {
        storeUsage.value= mapOf(
            storeDataTypes.CACHE to 100,
            storeDataTypes.FAVORITES to 210,
            storeDataTypes.FREE to 1000,
        )
    }

    fun clearStorage() {
        storeUsage.value= mapOf(
            storeDataTypes.CACHE to 0,
            storeDataTypes.FAVORITES to 0,
            storeDataTypes.FREE to getFreeDiskSpace(),
        )
    }

    fun getTotalDiskSpace():Long {
        val statFs = StatFs(Environment.getRootDirectory().absolutePath);
        return statFs.blockCountLong * statFs.blockSizeLong
    }
    private fun getFreeDiskSpace():Long {
        val statFs = StatFs(Environment.getDataDirectory().absolutePath);
        return statFs.freeBlocksLong * statFs.blockSizeLong
    }

    fun bytesToHuman(size: Long): String {
        val Kb:Long = (1 * 1024)
        val Mb = Kb * 1024
        val Gb = Mb * 1024
        val Tb = Gb * 1024
        val Pb = Tb * 1024
        val Eb = Pb * 1024
        if (size < Kb) return getString(context!!,R.string.size_bytes).format(size)
        if (size < Mb) return getString(context!!,R.string.size_Kb).format(size/Kb)
        if (size < Gb) return getString(context!!,R.string.size_Mb).format(size/Mb)
        if (size < Tb) return getString(context!!,R.string.size_Gb).format(size/Gb)
        if (size < Pb) return getString(context!!,R.string.size_Tb).format(size/Tb)
        return getString(context!!,R.string.size_Pb).format(size/Pb)
    }
}