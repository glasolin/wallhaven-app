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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.yield
import otus.gpb.homework.wallhaven.MainActivityViewModel
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.Settings
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
class UiData {
    private val tag = "UiData"
    private val settings = Settings()
    private var context: Context?=null
        get() {requireNotNull(field){println("Context was not initialized")};return field}

    private var coroutineScope:CoroutineScope?=null
        get() {requireNotNull(field){println("Coroutine scope was not initialized")};return field}

    private var pagesPool: CloseableCoroutineDispatcher?=null
        get() {requireNotNull(field){println("Pages pool was not initialized")};return field}

    var storeUsage = mutableStateOf<Map<StoreDataTypes,Long>>(emptyMap())
    private var currentRequestData=WHSearchRequest()

    var searchString = MutableLiveData<String>("")
    private var itemsOnPage = 0

    private var imageInfo = MutableSharedFlow<Pair<Int,Image>?>()
    private var pageInfo = MutableSharedFlow<Pair<Int,WHLoadingStatus>?>()

    var imagesData= mutableStateMapOf<Int,Image>()
    var pagesData = mutableStateMapOf<Int,WHLoadingStatus>()

    var jobs= mutableListOf<Job>()
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
        settings.setContext(context)
        imageFile.setCachePath(context.cacheDir)
    }

    fun settings():Settings=settings
    fun settingsLoaded():Boolean {
        if (!settings.isLoaded()) {
            settings.load()
            return false
        } else {
            return true
        }
    }
    fun load() {
        settings.load()
    }

    fun stopJobs() {
        jobs.forEach() {
            it.cancel()
        }
        jobs.clear()
    }

    private fun collect() {
        Log.d(tag,"collect")
        stopJobs()
        jobs.add(coroutineScope!!.launch {
            imageInfo.collect() {it ->
                yield()
                it?.let {(idx,img) ->
                    Log.d(tag,"image $idx updated to ${img.id}")
                    imagesData[idx]=img
                    loadImage(idx,WHFileType.THUMBNAIL)
                }
            }
        })
        jobs.add(coroutineScope!!.launch {
            pageInfo.collect() {it->
                yield()
                it?.let {(idx,status)->
                    Log.d(tag,"page $idx updated to status $status")
                    pagesData[idx]=status
                }
            }
        })
    }

    fun setCoroutineScope(scope: CoroutineScope) {
        coroutineScope = scope
        pagesPool = newSingleThreadContext("pagesPool")
        collect()
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

    private fun isPageLoaded(page:Int):Boolean {
        return (pagesData.containsKey(page) && pagesData[page]!=WHLoadingStatus.NONE)
    }

    private fun loadPage(page:Int=0) {
        if (isPageLoaded(page)) {
            return
        }
        pagesData[page]=WHLoadingStatus.LOADING
        coroutineScope!!.launch {
            Log.d(tag,"loading page info $page")
            val s= WHSearch()
            val list = s.search(currentRequestData.copy().apply {
                this.page=page
            })
            if (list == null ) {
                pageInfo.emit(Pair(page,WHLoadingStatus.NONE))
                return@launch
            }
            Log.d(tag,"page $page fetched")
            imagesTotal.intValue=list.meta.total
            itemsOnPage=list.meta.per_page

            for (i in 0 ..<min(list.meta.per_page,list.data.size)) {
                val idx=i+(list.meta.per_page*(list.meta.current_page-1))
                with (list.data[i]) {
                    //Log.d(tag,"emit image $idx as $id")
                    val(thumbWidth,thumbHeight)= WHGetThumbDimentions(dimension_x,dimension_y)
                    imageInfo.emit(Pair(idx,Image(
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
                        thumbStatus = MutableStateFlow(WHStatus.INFO),
                        imageStatus = MutableStateFlow(WHStatus.INFO),
                        thumbHeight = thumbHeight,
                        thumbWidth = thumbWidth,
                    )))
                }
            }
            //Log.d(tag,"emit page $page as ${WHLoadingStatus.LOADED.toString()}")
            pageInfo.emit(Pair(page,WHLoadingStatus.LOADED))
        }
    }

    fun reloadImages() {
        imagesTotal.intValue=0
        imagesData.clear()
        pagesData.clear()
        loadPage()
    }

    private fun imageInfoLoaded(id:Int):Boolean {
        return imagesData.containsKey(id)
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

    private fun loadImage(idx:Int, type: WHFileType) {
        fun updateStatus(status:WHStatus) {
            when (type) {
                WHFileType.IMAGE -> imagesData[idx]!!.imageStatus.value=status
                WHFileType.THUMBNAIL -> imagesData[idx]!!.thumbStatus.value=status
            }
        }
        fun getStatus():WHStatus {
            return when (type) {
                WHFileType.IMAGE -> imagesData[idx]!!.imageStatus.value
                WHFileType.THUMBNAIL -> imagesData[idx]!!.thumbStatus.value
            }
        }
        coroutineScope!!.launch {
            imagesData[idx]?.let {
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
}

