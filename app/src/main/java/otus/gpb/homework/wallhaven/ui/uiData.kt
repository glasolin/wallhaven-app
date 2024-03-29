package otus.gpb.homework.wallhaven.ui

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.wh.emptyImage
import otus.gpb.homework.wallhaven.wh.Image
import otus.gpb.homework.wallhaven.wh.WHCategories
import otus.gpb.homework.wallhaven.wh.WHColor
import otus.gpb.homework.wallhaven.wh.WHFileType
import otus.gpb.homework.wallhaven.wh.WHGetThumbDimentions
import otus.gpb.homework.wallhaven.wh.WHImage
import otus.gpb.homework.wallhaven.wh.WHLoadingStatus
import otus.gpb.homework.wallhaven.wh.WHPurity
import otus.gpb.homework.wallhaven.wh.WHSearch
import otus.gpb.homework.wallhaven.wh.WHSearchApi
import otus.gpb.homework.wallhaven.wh.WHSearchRequest
import otus.gpb.homework.wallhaven.wh.WHStatus
import retrofit2.http.Url
import java.io.File
import java.time.Duration
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

enum class StoreDataTypes {
    NONE, FAVORITES, CACHE, FREE
}
data class pageData(val page:Int,val data:List<Image>)

class UiData {
    private val tag = "UiData"
    private var context: Context?=null
        get() {requireNotNull(field){println("Context was not initialized")};return field}

    private var coroutineScope:CoroutineScope?=null
        get() {requireNotNull(field){println("Coroutine scope was not initialized")};return field}

    private var pagesPool: CloseableCoroutineDispatcher?=null
        get() {requireNotNull(field){println("Pages pool was not initialized")};return field}

    var storeUsage = mutableStateOf<Map<StoreDataTypes,Long>>(emptyMap())
    private var currentRequestData=WHSearchRequest()

    private var loadedPages = mutableMapOf<Int,WHLoadingStatus>()
    var searchString = MutableLiveData<String>("")
    private var itemsOnPage = 0

    private val _imagesData= MutableStateFlow<Map<Int,Image>>(emptyMap())
    val imagesData=_imagesData.asStateFlow()
    val imagesTotal= mutableIntStateOf(0)

    private val imageFile=WHImage()

    init {
        updateStorageUsage()
        searchString.observeForever {
            currentRequestData.search=it
        }

    }
    fun setContext(context: Context) {
        this.context=context
        imageFile.setCachePath(context.cacheDir)
    }

    fun setCoroutineScope(scope: CoroutineScope) {
        coroutineScope=scope
        pagesPool = newSingleThreadContext("pagesPool")
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

    fun loadPage(page:Int=0) {
        if (loadedPages.containsKey(page) && loadedPages[page]!=WHLoadingStatus.NONE) {
            return
        }
        loadedPages[page]=WHLoadingStatus.LOADING
        coroutineScope!!.launch {
            Log.d(tag,"loading page info $page")
            val s= WHSearch()
            val list = s.search(currentRequestData.copy().apply {
                this.page=page
            })
            if (list == null ) {
                loadedPages[page]=WHLoadingStatus.NONE
                return@launch
            }
            Log.d(tag,"page $page fetched")
            imagesTotal.intValue=list.meta.total
            itemsOnPage=list.meta.per_page
            val out=mutableMapOf<Int,Image>()

            for (i in 0 ..<min(list.meta.per_page,list.data.size)) {
                val idx=i+(list.meta.per_page*(list.meta.current_page-1))
                with (list.data[i]) {
                    //Log.d(tag,"loading image info $i to position $shift")
                    val(thumbWidth,thumbHeight)= WHGetThumbDimentions(dimension_x,dimension_y)
                    out[idx] = Image(
                        id = id,
                        imagePath = path,
                        thumbPath = thumbs.original,
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
                        thumbStatus = mutableStateOf(WHStatus.INFO),
                        imageStatus = mutableStateOf(WHStatus.INFO),
                        thumbHeight = thumbHeight,
                        thumbWidth = thumbWidth,
                    )
                }
            }
            _imagesData.value=out
            loadThumbnails()
            loadedPages[page]=WHLoadingStatus.LOADED
        }
    }

    fun reloadImages() {
        _imagesData.value= emptyMap()
        loadedPages.clear()
        loadPage()
    }

    private fun imageInfoLoaded(id:Int):Boolean {
        return _imagesData.value.containsKey(id)
    }

    fun imageFromCache(id:String,type: WHFileType):String {
        return imageFile.fromCache(id,type).absolutePath
    }

    private fun imageInCache(id: String,type: WHFileType):Boolean {
        return imageFile.inCache(id,type)
    }

    fun loadImageInfo(id: Int) {
        if (imageInfoLoaded(id)) {
            return
        } else if (itemsOnPage > 0) {
            val page = id.floorDiv(itemsOnPage)
            loadPage(page)
        }
    }

    private fun loadThumbnails() {
        //loadImage(idx,WHFileType.THUMBNAIL)
    }

    private fun loadImage(idx:Int,type: WHFileType) {
        fun updateStatus(status:WHStatus) {
            when (type) {
                WHFileType.IMAGE -> _imagesData.value[idx]!!.imageStatus.value=status
                WHFileType.THUMBNAIL -> _imagesData.value[idx]!!.thumbStatus.value=status
            }
        }
        fun getStatus():WHStatus {
            return when (type) {
                WHFileType.IMAGE -> _imagesData.value[idx]!!.imageStatus.value
                WHFileType.THUMBNAIL -> _imagesData.value[idx]!!.thumbStatus.value
            }
        }
        _imagesData.value[idx]?.let {
            if (getStatus() == WHStatus.INFO) {
                updateStatus(WHStatus.LOADING)
                coroutineScope!!.launch {
                    var v = WHStatus.INFO
                    if (imageInCache(it.id, type)) {
                        v = WHStatus.LOADED
                    } else {
                        updateStatus(WHStatus.LOADING)
                        when (type) {
                            WHFileType.IMAGE ->
                                if (imageFile.toCache(it.id, type, it.imagePath).isNotEmpty()) {
                                    v = WHStatus.LOADED
                                }

                            WHFileType.THUMBNAIL ->
                                if (imageFile.toCache(it.id, type, it.imagePath).isNotEmpty()) {
                                    v = WHStatus.LOADED
                                }
                        }
                        updateStatus(v)
                    }
                }
            }
        }
    }
}

