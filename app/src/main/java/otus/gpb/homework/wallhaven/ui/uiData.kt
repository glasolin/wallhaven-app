package otus.gpb.homework.wallhaven.ui

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.wh.emptyImage
import otus.gpb.homework.wallhaven.wh.Image
import otus.gpb.homework.wallhaven.wh.WHCategories
import otus.gpb.homework.wallhaven.wh.WHColor
import otus.gpb.homework.wallhaven.wh.WHPurity
import otus.gpb.homework.wallhaven.wh.WHSearch
import otus.gpb.homework.wallhaven.wh.WHSearchApi
import otus.gpb.homework.wallhaven.wh.WHSearchRequest
import otus.gpb.homework.wallhaven.wh.WHStatus
import java.io.File
import java.time.Duration
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

enum class StoreDataTypes {
    NONE, FAVORITES, CACHE, FREE
}

class UiData {
    private val tag = "UiData"
    private var context: Context?=null
        get() {requireNotNull(field){println("Context was not initialized")};return field}

    private var coroutineScope:CoroutineScope?=null
        get() {requireNotNull(field){println("Coroutine scope was not initialized")};return field}

    var storeUsage = mutableStateOf<Map<StoreDataTypes,Long>>(emptyMap())
    var searchString = MutableLiveData<String>("")
    private val _imagesData= MutableStateFlow<MutableList<Image>>(mutableListOf())
    val imagesData=_imagesData.asStateFlow()
    private var currentRequestData=WHSearchRequest()

    init {
        updateStorageUsage()
        searchString.observeForever {
            currentRequestData.search=it
        }

    }
    fun setContext(context: Context) {
        this.context=context
    }

    fun setCoroutineScope(scope: CoroutineScope) {
        coroutineScope=scope
    }
    private fun updateStorageUsage() {
        storeUsage.value= mapOf(
            StoreDataTypes.CACHE to 100,
            StoreDataTypes.FAVORITES to 210,
            StoreDataTypes.FREE to 1000,
        )
    }

    fun clearStorage() {
        storeUsage.value= mapOf(
            StoreDataTypes.CACHE to 0,
            StoreDataTypes.FAVORITES to 0,
            StoreDataTypes.FREE to getFreeDiskSpace(),
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
        try {
            val Kb: Long = (1 * 1024)
            val Mb = Kb * 1024
            val Gb = Mb * 1024
            val Tb = Gb * 1024
            val Pb = Tb * 1024
            val Eb = Pb * 1024
            if (size < Kb) return getString(context!!, R.string.size_bytes).format(size)
            if (size < Mb) return getString(context!!, R.string.size_Kb).format(size / Kb)
            if (size < Gb) return getString(context!!, R.string.size_Mb).format(size / Mb)
            if (size < Tb) return getString(context!!, R.string.size_Gb).format(size / Gb)
            if (size < Pb) return getString(context!!, R.string.size_Tb).format(size / Tb)
            if (size < Eb) return getString(context!!, R.string.size_Eb).format(size / Pb)
            return getString(context!!, R.string.size_Eb).format(size / Eb)
        } finally {
            return "0"
        }
    }

    fun loadImages() {
        coroutineScope!!.launch {
            val s= WHSearch()
            val list= s.search(currentRequestData) ?: return@launch
            if (_imagesData.value.size == 0) {
                for (i in 0..<list.meta.total) {
                    _imagesData.value.add(emptyImage())
                }
            }
            for (i in 0 ..<min(list.meta.per_page,list.data.size)) {
                val shift=i+(list.meta.per_page*(list.meta.current_page-1))
                while (shift>=_imagesData.value.size) {
                    _imagesData.value.add(emptyImage())
                }
                with (list.data[i]) {
                    Log.d(tag,"loading image info $i to position $shift")
                    _imagesData.value[shift] = Image(
                        id = id,
                        path = path,
                        category = WHCategories.fromString(category),
                        colors = colors.map { WHColor.fromString(it) },
                        width = dimension_x,
                        height = dimension_y,
                        purity = WHPurity.fromString(purity),
                        ratio = ratio,
                        resolution = resolution,
                        size = file_size,
                        source = source,
                        views = views,
                        status = WHStatus.INFO,
                    )
                }
            }
        }
    }

    fun reloadImages() {
        _imagesData.value.clear()
        loadImages()
    }
}