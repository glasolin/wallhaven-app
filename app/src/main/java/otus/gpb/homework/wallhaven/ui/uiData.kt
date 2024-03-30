package otus.gpb.homework.wallhaven.ui

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.yield
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.Settings
import otus.gpb.homework.wallhaven.wh.ImageInfo
import otus.gpb.homework.wallhaven.wh.WHCategories
import otus.gpb.homework.wallhaven.wh.WHColor
import otus.gpb.homework.wallhaven.wh.WHFileType
import otus.gpb.homework.wallhaven.wh.WHGetThumbDimentions
import otus.gpb.homework.wallhaven.wh.WHImage
import otus.gpb.homework.wallhaven.wh.WHLoadingStatus
import otus.gpb.homework.wallhaven.wh.WHPurity
import otus.gpb.homework.wallhaven.wh.WHRatio
import otus.gpb.homework.wallhaven.wh.WHSearch
import otus.gpb.homework.wallhaven.wh.WHSearchRequest
import otus.gpb.homework.wallhaven.wh.WHStatus
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.math.min

enum class StoreDataTypes {
    NONE, FAVORITES, CACHE, FREE
}
class UiData {
    private val tag = "UiData"
    private val tpTag = "UIDataThreadPool"
    private val settings = Settings()
    private var threadPool: ExecutorService
    private var context: Context?=null
        get() {requireNotNull(field){println("Context was not initialized")};return field}

    private var coroutineScope:CoroutineScope?=null
        get() {requireNotNull(field){println("Coroutine scope was not initialized")};return field}

    private var cachePath: File?=null
        get() {requireNotNull(field){println("Cache path was not initialized")};return field}

    var storeUsage = mutableStateOf<Map<StoreDataTypes,Long>>(emptyMap())
    private var currentRequestData=WHSearchRequest()

    var searchString = MutableLiveData<String>("")
    private var itemsOnPage = 0

    private var imageInfo = MutableSharedFlow<Pair<Int,ImageInfo>?>()
    private var pageInfo = MutableSharedFlow<Pair<Int,WHLoadingStatus>?>()

    var imagesData= mutableStateMapOf<Int,ImageInfo>()
    var pagesData = mutableStateMapOf<Int,WHLoadingStatus>()

    private var jobs= mutableListOf<Job>()
    val imagesTotal= mutableIntStateOf(-1)
    private var ready=false


    init {
        val corePoolSize = 4
        val maximumPoolSize = corePoolSize * 4
        val keepAliveTime = 100L
        val workQueue = SynchronousQueue<Runnable>()
        threadPool = ThreadPoolExecutor(
            corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue
        )

        updateStorageUsage()
        searchString.observeForever {
            currentRequestData.search=it
            refresh()
        }
        settings.order.observeForever() {
            currentRequestData.order=it
            refresh()
        }
        settings.sorting.observeForever() {
            currentRequestData.sorting=it
            refresh()
        }

        settings.whCatehory.observeForever() {
            currentRequestData.category = it
            refresh()
        }
        settings.whPurity.observeForever() {
            currentRequestData.purity = it
            refresh()
        }

        settings.whRatio.observeForever() {
            currentRequestData.ratio = it
            refresh()
        }

        settings.apiKey.observeForever() {
            currentRequestData.apiKey=it
            refresh()
        }

        settings.whResolutionWidth.observeForever() {
            currentRequestData.width=it
            refresh()
        }

        settings.whResolutionHeight.observeForever() {
            currentRequestData.height=it
            refresh()
        }
        settings.whColor.observeForever() {
            currentRequestData.color=WHColor.fromString(it)
            refresh()
        }
    }

    fun setContext(context: Context) {
        this.context=context
        settings.setContext(context)
        if (context.cacheDir!=null) {
            cachePath=context.cacheDir
        }
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

    private fun stopJobs() {
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
                    loadImage(img.copy(), idx, WHFileType.THUMBNAIL)
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
        collect()
        ready=true
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
        return (pagesData.containsKey(page) && pagesData[page]!=WHLoadingStatus.NONE) && (pagesData[page]!=WHLoadingStatus.FAILED)
    }
    fun loadImageInfo(id: Int) {
        if (!imageInfoLoaded(id)) {
            loadPage(imagePage(id))
        }
    }
    fun loadPage(page:Int=0) {
        Log.d(tpTag,"loadPage in $page")
        threadPool.execute {
            loadPageT(page)
        }
        Log.d(tpTag,"loadPage out $page")
    }
    private fun loadPageT(page:Int=0) {
        if (isPageLoaded(page)) {
            return
        }
        pagesData[page]=WHLoadingStatus.LOADING
        coroutineScope!!.launch {
            Log.d(tag,"loading page info $page")
            val s= WHSearch()
            val list = s.search(currentRequestData.copy().apply {
                this.page=page+1
            })
            if (list == null ) {
                pageInfo.emit(Pair(page,WHLoadingStatus.NONE))
                return@launch
            }
            Log.d(tag,"page $page fetched")
            imagesTotal.intValue=list.meta.total
            itemsOnPage=list.meta.per_page
            list.meta.seed?.let {
                currentRequestData.seed=it
            }

            for (i in 0 ..<min(list.meta.per_page,list.data.size)) {
                val idx=i+(list.meta.per_page*(list.meta.current_page-1))
                with (list.data[i]) {
                    //Log.d(tag,"emit image $idx as $id")
                    val(thumbWidth,thumbHeight)= WHGetThumbDimentions(dimension_x,dimension_y)
                    imageInfo.emit(Pair(idx, ImageInfo(
                        id = id,
                        imagePath = path,
                        thumbPath = thumbs.small,
                        category = WHCategories.fromString(category),
                        colors = colors.map { WHColor.fromString(it) },
                        width = dimension_x,
                        height = dimension_y,
                        purity = WHPurity.fromString(purity),
                        ratio = WHRatio.fromString(ratio,dimension_x,dimension_y),
                        resolution = resolution,
                        size = file_size,
                        source = source,
                        views = views,
                        thumbStatus = WHStatus.INFO,
                        imageStatus = WHStatus.INFO,
                        thumbHeight = thumbHeight,
                        thumbWidth = thumbWidth,
                    )
                    ))
                }
            }
            //Log.d(tag,"emit page $page as ${WHLoadingStatus.LOADED.toString()}")
            pageInfo.emit(Pair(page,WHLoadingStatus.LOADED))
        }
    }

    fun refresh() {
        imagesTotal.intValue=-1
        imagesData.clear()
        pagesData.clear()
        if (ready) {
            loadPage()
        }
    }

    private fun imageInfoLoaded(id:Int):Boolean {
        return imagesData.containsKey(id)
    }

    fun imageFromCache(id:String,type: WHFileType):String {
        val f=WHImage().apply { setCachePath(cachePath!!) }
        return f.fromCache(id,type).absolutePath
    }

    private fun imageInCache(id: String,type: WHFileType):Boolean {
        val f=WHImage().apply { setCachePath(cachePath!!) }
        return f.inCache(id,type)
    }

    fun imagePage(id: Int):Int {
        return if (itemsOnPage > 0) id.floorDiv(itemsOnPage) else 0
    }

    private fun loadImage(img:ImageInfo,idx:Int, type: WHFileType) {
        Log.d(tpTag,"loadImage in $idx")
        threadPool.execute{
            loadImageT(img,idx,type)
        }
        Log.d(tpTag,"loadImage out $idx")
    }
    private fun loadImageT(img:ImageInfo,idx:Int, type: WHFileType) {
        fun updateStatus(status:WHStatus) {
            when (type) {
                WHFileType.IMAGE -> img.imageStatus=status
                WHFileType.THUMBNAIL -> img.thumbStatus=status
            }
        }
        fun getStatus():WHStatus {
            return when (type) {
                WHFileType.IMAGE -> img.imageStatus
                WHFileType.THUMBNAIL -> img.thumbStatus
            }
        }
        Log.d(tag,"Image $idx checking for loading")

           // Log.d(tag,"Image $idx in imagesData collection")
        when (getStatus()) {
            WHStatus.NONE, WHStatus.INFO -> {
                updateStatus(WHStatus.LOADING)
                coroutineScope!!.launch {
                    var v = WHStatus.INFO
                    if (imageInCache(img.id, type)) {
                        //  Log.d(tag,"Image $idx in cache")
                        v = WHStatus.LOADED
                    } else {
                        when (type) {
                            WHFileType.IMAGE -> {
                                val f = WHImage().apply { setCachePath(cachePath!!) }
                                if (f.toCache(img.id, type, img.imagePath).isNotEmpty()) {
                                    v = WHStatus.LOADED
                                } else {
                                    v = WHStatus.ERROR
                                }
                            }
                            WHFileType.THUMBNAIL -> {
                                //  Log.d(tag, "Image $idx to cache called")
                                val f=WHImage().apply { setCachePath(cachePath!!) }
                                if (f.toCache(img.id, type, img.imagePath).isNotEmpty()) {
                                    v = WHStatus.LOADED
                                } else {
                                    v = WHStatus.ERROR
                                }
                            }
                        }
                    }
                    Log.d(tag, "Image $idx set status to $v")
                    updateStatus(v)
                    imageInfo.emit(Pair(idx,img))
                }
            }
            WHStatus.ERROR,WHStatus.LOADING,WHStatus.LOADED -> {}
        }
    }
}

