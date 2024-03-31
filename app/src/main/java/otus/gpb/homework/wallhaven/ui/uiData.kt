package otus.gpb.homework .wallhaven.ui

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateList
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.yield
import otus.gpb.homework.wallhaven.R
import otus.gpb.homework.wallhaven.Settings
import otus.gpb.homework.wallhaven.fav.Favorites
import otus.gpb.homework.wallhaven.wh.ImageInfo
import otus.gpb.homework.wallhaven.wh.WHCategories
import otus.gpb.homework.wallhaven.wh.WHColor
import otus.gpb.homework.wallhaven.wh.WHFileType
import otus.gpb.homework.wallhaven.wh.WHGetThumbDimentions
import otus.gpb.homework.wallhaven.wh.WHImage
import otus.gpb.homework.wallhaven.wh.WHImageExtendedInfo
import otus.gpb.homework.wallhaven.wh.WHLoadingStatus
import otus.gpb.homework.wallhaven.wh.WHPurity
import otus.gpb.homework.wallhaven.wh.WHRatio
import otus.gpb.homework.wallhaven.wh.WHSearch
import otus.gpb.homework.wallhaven.wh.WHSearchRequest
import otus.gpb.homework.wallhaven.wh.WHStatus
import otus.gpb.homework.wallhaven.wh.WHTag
import otus.gpb.homework.wallhaven.wh.WHTagsSuggestion
import java.io.File
import java.time.Instant
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.random.Random

enum class StoreDataTypes {
    NONE, FAVORITES, CACHE, FREE
}

enum class UpdateParts {
    ALL, THUMB_INFO, IMAGE_INFO, EXTENDED_INFO, FAVORITE_INFO
}

const val IMAGE_LOAD_TRIES = 3
const val IMAGE_LOAD_MIN_DELAY_MS = 500
const val IMAGE_LOAD_MAX_DELAY_MS = 1500
const val IMAGE_RELOAD_DELAY_SEC = 3

const val FORCE_IMAGE_LOAD_DELAY = 500
const val FORCE_PAGE_LOAD_DELAY = 500


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

    private var imageInfo = MutableSharedFlow<Triple<Int,ImageInfo,UpdateParts>?>()
    private var pageInfo = MutableSharedFlow<Pair<Int,WHLoadingStatus>?>()

    var imagesData= mutableStateMapOf<Int,ImageInfo>()
    var pagesData = mutableStateMapOf<Int,WHLoadingStatus>()

    private var suggester = MutableSharedFlow<List<String>>()
    var tagSuggestion= mutableStateOf<List<String>>(emptyList())

    private var jobs= mutableListOf<Job>()
    val imagesTotal= mutableIntStateOf(-1)
    val selectedImage = mutableIntStateOf(-1)
    private var ready=false
    private var nopages = false
    var dynamicTitle = mutableStateOf<String>("")


    init {
        val corePoolSize = 4
        val maximumPoolSize = corePoolSize * 4
        val keepAliveTime = 1000L
        //val workQueue = SynchronousQueue<Runnable>()
        val workQueue = LinkedBlockingQueue<Runnable>()
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

        settings.whTags.observeForever() {
            currentRequestData.tags=it
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

    private fun updateParts(info: Triple<Int,ImageInfo,UpdateParts>?) {
        fun get(): SnapshotStateMap<Int, ImageInfo> {
            return imagesData
        }
        fun getByIds(idx:Int):ImageInfo {
            return imagesData[idx]!!
        }
        fun setById(idx:Int,img:ImageInfo) {
            imagesData[idx]=img
        }
        fun isNullById(idx: Int):Boolean {
            return (imagesData[idx] == null)
        }
        info?.let {(idx,img,upd) ->
            Log.d(tag,"image $idx updated to ${img.id}")
            img.index=idx
            if (isNullById(idx)) {
                setById(idx,img)
            }
            when (upd) {
                UpdateParts.ALL -> setById(idx,img)
                UpdateParts.THUMB_INFO -> setById(idx,getByIds(idx).copy(
                        thumbStatus = img.thumbStatus,
                    ))
                UpdateParts.IMAGE_INFO -> {
                    setById(idx,getByIds(idx).copy(
                        imageStatus=img.imageStatus,
                    ))
                }
                UpdateParts.EXTENDED_INFO -> {
                    setById(idx,getByIds(idx).copy(
                        extendedInfoStatus = img.extendedInfoStatus,
                        tags = img.tags,
                    ))
                }
                UpdateParts.FAVORITE_INFO -> {
                    setById(idx,getByIds(idx).copy(
                        inFavorites = img.inFavorites,
                    ))
                }
            }
            loadImage(getByIds(idx).copy(), WHFileType.THUMBNAIL)
        }
        if (IMAGE_RELOAD_DELAY_SEC > 0) {
            get().filter { (_, img) ->
                ((img.thumbStatus == WHStatus.ERROR)
                        || (img.imageStatus == WHStatus.ERROR))
                        && (Instant.now().epochSecond - img.updated.epochSecond > IMAGE_RELOAD_DELAY_SEC)
            }.forEach() { (i, img) ->
                Log.d(tag, "calling image reload for $i")
                loadImage(img.copy(), WHFileType.THUMBNAIL)
            }
        }
    }

    private fun collect() {
        Log.d(tag,"collect")
        stopJobs()
        jobs.add(coroutineScope!!.launch {
            imageInfo.collect() {it ->
                yield()
                updateParts(it)
            }
        })
        jobs.add(coroutineScope!!.launch {
            pageInfo.collect() {it->
                yield()
                if (!nopages) {
                    it?.let { (idx, status) ->
                        //Log.d(tag,"page $idx updated to status $status")
                        pagesData[idx] = status
                    }
                }
            }
        })
        jobs.add(coroutineScope!!.launch {
            suggester.collect() {
                //Log.d(tag,"Added ${it} to suggestion list")
                tagSuggestion.value=it
            }
        })
    }

    fun tagsSuggest(term: String) {
        jobs.add(coroutineScope!!.launch() {
            WHTagsSuggestion().suggest(term)?.let {tags ->
                //Log.d(tag,"Suggestion list cleared")
                suggester.emit(tags.results.map{
                    it.value
                })
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

    private fun loadPage(page:Int=0) {
        if (nopages) {
            return
        }
        if (isPageLoaded(page)) {
            return
        }
        if (pagesData[page]==WHLoadingStatus.LOADING) {
            return
        }
        threadPool.execute {
            loadPageT(page)
        }
    }

    private fun loadPageT(page:Int=0) {
        if (isPageLoaded(page)) {
            return
        }
        if (pagesData[page]==WHLoadingStatus.LOADING) {
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
                if (currentRequestData.seed.isNullOrEmpty()) {
                    currentRequestData.seed = it
                }
            }

            for (i in 0 ..<min(list.meta.per_page,list.data.size)) {
                val idx=i+(list.meta.per_page*(list.meta.current_page-1))
                with (list.data[i]) {
                    //Log.d(tag,"emit image $idx as $id")
                    val(thumbWidth,thumbHeight)= WHGetThumbDimentions(dimension_x,dimension_y)
                    imageInfo.emit(
                        Triple(idx,
                            ImageInfo(
                                index=0,
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
                                tags = emptyList(),
                                thumbStatus = WHStatus.INFO,
                                imageStatus = WHStatus.INFO,
                                extendedInfoStatus = WHStatus.NONE,
                                thumbHeight = thumbHeight,
                                thumbWidth = thumbWidth,
                                updated = Instant.now(),
                                inFavorites = false,
                            )
                            ,UpdateParts.ALL)
                    )
                }
            }
            //Log.d(tag,"emit page $page as ${WHLoadingStatus.LOADED.toString()}")
            delay(FORCE_PAGE_LOAD_DELAY.toLong())
            pageInfo.emit(Pair(page,WHLoadingStatus.LOADED))
        }
    }

    private fun loadExtendedInfo(img:ImageInfo) {
        if (!imageInfoLoaded(img.index)) {
            return
        }
        if (
            (img.extendedInfoStatus == WHStatus.LOADING)
            || (img.extendedInfoStatus == WHStatus.LOADED)
        ){
            return
        }
        updateExtendedInfoLoadingStatus(img, WHStatus.LOADING)
        threadPool.execute {
            loadExtendedInfoT(img)
        }
    }

    private fun loadExtendedInfoT(img:ImageInfo) {
        coroutineScope!!.launch {
            val info =
                WHImageExtendedInfo(currentRequestData.apiKey).fetchTagsInfo(img.id)
            val newImageInfo = img.copy(
                extendedInfoStatus = WHStatus.LOADED,
                tags = info
            )
            newImageInfo.updated=Instant.now()
            imageInfo.emit(Triple(newImageInfo.index,newImageInfo,UpdateParts.EXTENDED_INFO))
        }
    }

    fun refresh() {
        nopages=false
        clear()
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

    private fun updateExtendedInfoLoadingStatus(img:ImageInfo, status: WHStatus) {
        imagesData[img.index]?.extendedInfoStatus=status
    }
    private fun updateImageLoadingStatus(img:ImageInfo, type:WHFileType, status: WHStatus) {
        when (type) {
            WHFileType.IMAGE -> imagesData[img.index]?.imageStatus=status
            WHFileType.THUMBNAIL -> imagesData[img.index]?.thumbStatus=status
        }
    }

    private fun loadImage(img:ImageInfo, type: WHFileType) {
        threadPool.execute{
            loadImageT(img,type)
        }
    }
    private fun loadImageT(img:ImageInfo, type: WHFileType) {
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
        fun getPath():String {
            return when (type) {
                WHFileType.IMAGE -> img.imagePath
                WHFileType.THUMBNAIL -> img.thumbPath
            }
        }
        Log.d(tag,"Image ${img.index} checking for loading")

           // Log.d(tag,"Image $idx in imagesData collection")
        when (getStatus()) {
            WHStatus.NONE, WHStatus.INFO -> {
                updateImageLoadingStatus(img,type,WHStatus.LOADING)
                coroutineScope!!.launch {
                    var v = WHStatus.INFO
                    if (imageInCache(img.id, type)) {
                        //  Log.d(tag,"Image $idx in cache")
                        v = WHStatus.LOADED
                    } else {
                        //  Log.d(tag, "Image $idx to cache called")
                        val f=WHImage().apply { setCachePath(cachePath!!) }
                        var tries= IMAGE_LOAD_TRIES
                        while (f.toCache(img.id, type, getPath()).isEmpty()) {
                            Log.d(tag,"Image ${img.index} try ${tries}")
                            tries--
                            if (tries == 0) {break}
                            delay(Random.nextInt(IMAGE_LOAD_MIN_DELAY_MS, IMAGE_LOAD_MAX_DELAY_MS).toLong())
                        }
                        v = if (f.inCache(img.id,type)) {
                            WHStatus.LOADED
                        } else {
                            WHStatus.ERROR
                        }
                    }
                    Log.d(tag, "Image ${img.index} set status to $v")
                    updateStatus(v)
                    img.updated=Instant.now()
                    delay(FORCE_IMAGE_LOAD_DELAY.toLong())
                    when (type) {
                        WHFileType.IMAGE -> imageInfo.emit(Triple(img.index,img,UpdateParts.IMAGE_INFO))
                        WHFileType.THUMBNAIL -> imageInfo.emit(Triple(img.index,img,UpdateParts.THUMB_INFO))
                    }
                }
            }
            WHStatus.ERROR,WHStatus.LOADING,WHStatus.LOADED -> {}
        }
    }

    fun clearFilters() {
        settings.whCatehory.value=WHCategories.GENERAL
        settings.whRatio.value=WHRatio.ANY
        settings.whResolutionWidth.value=0
        settings.whResolutionHeight.value=0
        settings.whColor.value=""
        settings.whPurity.value=WHPurity.SFW
        settings.whTags.value= emptyList()
    }

    fun selectImage(idx: Int) {
        selectedImage.intValue=idx
        Log.d(tag,"selected image is $idx")
        imagesData[idx]?.let {
            dynamicTitle.value="${it.width}x${it.height}"
            loadImage(it.copy(),WHFileType.IMAGE,)
            inFavorites(it.copy())
            loadExtendedInfo(it.copy())
        }
    }

    fun loadFromColor(color: WHColor) {
        settings.whColor.value=color.name
        refresh()
    }

    fun loadFromTag(tag: WHTag) {
        settings.whTags.value= mutableListOf(tag.tag)
        searchString.value=""
        refresh()
    }

    fun addToFavorites(img:ImageInfo) {
        coroutineScope!!.launch {
            Favorites(context!!).add(img.id)
            imageInfo.emit(
                Triple(img.index,img.copy(inFavorites = true),UpdateParts.FAVORITE_INFO)
            )
        }
    }
    fun inFavorites(img:ImageInfo) {
        coroutineScope!!.launch {
            val rc=Favorites(context!!).exists(img.id)
            imageInfo.emit(
                Triple(img.index,img.copy(inFavorites = rc),UpdateParts.FAVORITE_INFO)
            )
        }
    }

    fun removeFromFavorites(img:ImageInfo) {
        coroutineScope!!.launch {
            Favorites(context!!).remove(img.id)
            imageInfo.emit(
                Triple(img.index, img.copy(inFavorites = false),UpdateParts.FAVORITE_INFO)
            )
        }
    }

    private fun loadFavorite(idx:Int, id:String) {
        threadPool.execute() {
            coroutineScope!!.launch {
                val ei=WHImageExtendedInfo(currentRequestData.apiKey)
                val info =ei.fetchExtendedInfo(id)
                info?.let {
                    imageInfo.emit(
                        with (it.data) {
                            val(thumbWidth,thumbHeight)= WHGetThumbDimentions(dimension_x,dimension_y)
                            Triple(
                                idx,
                                ImageInfo(
                                    index = idx,
                                    id = id,
                                    imagePath = path,
                                    thumbPath = thumbs.small,
                                    category = WHCategories.fromString(category),
                                    colors = colors.map { WHColor.fromString(it) },
                                    width = dimension_x,
                                    height = dimension_y,
                                    purity = WHPurity.fromString(purity),
                                    ratio = WHRatio.fromString(ratio, dimension_x, dimension_y),
                                    resolution = resolution,
                                    size = file_size,
                                    source = source,
                                    views = views,
                                    tags = ei.tagsFromExtendedInfo(info),
                                    thumbStatus = WHStatus.INFO,
                                    imageStatus = WHStatus.INFO,
                                    extendedInfoStatus = WHStatus.LOADED,
                                    thumbHeight = thumbHeight,
                                    thumbWidth = thumbWidth,
                                    updated = Instant.now(),
                                    inFavorites = false,
                                ), UpdateParts.ALL
                            )
                        }
                    )
                }
            }
        }
    }

    private fun clear() {
        imagesTotal.intValue=-1
        imagesData.clear()
        pagesData.clear()
    }

    fun favoritesList() {
        clear()
        nopages=true
        coroutineScope!!.launch {
            val list=Favorites(context!!).fetch()
            imagesTotal.intValue=list.size
            list.forEachIndexed{idx,id ->
                val page=imagePage(idx)
                pageInfo.emit(Pair(page,WHLoadingStatus.LOADED))
                loadFavorite(idx,id)
            }
        }
    }

    fun clearFavorites() {
        coroutineScope!!.launch {
            Favorites(context!!).removeAll()
            favoritesList()
        }
    }
}

