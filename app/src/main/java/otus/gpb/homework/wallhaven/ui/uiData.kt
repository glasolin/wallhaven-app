package otus.gpb.homework .wallhaven.ui

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.asIntState
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.flow.asStateFlow
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
import otus.gpb.homework.wallhaven.wh.WHPageStatus
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

enum class ImageMode {
    SEARCH, FAVORITES
}

enum class PossibleFilters {
    CATEGORY,RATIO,RESOLUTION,COLOR,PURITY,TAGS,SEARCH,IMAGE
}

const val IMAGE_LOAD_TRIES = 3
const val IMAGE_LOAD_MIN_DELAY_MS = 500
const val IMAGE_LOAD_MAX_DELAY_MS = 1500
const val IMAGE_RELOAD_DELAY_SEC = 3

const val FORCE_IMAGE_LOAD_DELAY = 0
const val FORCE_PAGE_LOAD_DELAY = 0


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

    private var dataPath: File?=null
        get() {requireNotNull(field){println("Data path was not initialized")};return field}

    var storeUsage = mutableStateOf<Map<StoreDataTypes,Long>>(emptyMap())
    private var currentRequestData=WHSearchRequest()

    var searchString = MutableLiveData<String>("")
    var fromImageString = MutableLiveData<String>("")
    private var itemsOnPage = 0

    private var imageInfo = MutableSharedFlow<Triple<Int,ImageInfo,UpdateParts>?>()
    private var pageInfo = MutableSharedFlow<Pair<Int,WHPageStatus>?>()

    private var _favoritesData=MutableStateFlow<List<ImageInfo>>(emptyList())
    val favoritesData=_favoritesData.asStateFlow()
    var imagesData=mutableStateMapOf<Int,ImageInfo>()
    var pagesData =mutableStateMapOf<Int,WHPageStatus>()

    private var suggester = MutableSharedFlow<List<String>>()
    var tagSuggestion= mutableStateOf<List<String>>(emptyList())

    private var jobs= mutableListOf<Job>()
    val imagesTotal= mutableIntStateOf(-1)
    val selectedImage = mutableStateOf<ImageInfo?>(null)
    val nextImage = mutableStateOf<ImageInfo?>(null)
    val previousImage = mutableStateOf<ImageInfo?>(null)
    private var ready=false

    var imageMode=ImageMode.SEARCH

    private val favorites=Favorites()
    private val whImg=WHImage()


    init {
        val corePoolSize = 4
        val maximumPoolSize = corePoolSize * 4
        val keepAliveTime = 1000L
        //val workQueue = SynchronousQueue<Runnable>()
        val workQueue = LinkedBlockingQueue<Runnable>()
        threadPool = ThreadPoolExecutor(
            corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue
        )

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
        fromImageString.observeForever() {
            currentRequestData.image_id=it
            refresh()
        }


    }

    fun setContext(context: Context) {
        this.context=context
        settings.setContext(context)
        if (context.cacheDir!=null) {
            cachePath=context.cacheDir
        }
        if (context.dataDir!=null) {
            dataPath=context.dataDir
        }
        favorites.setContext(context)
        favorites.setPath(cachePath!!,dataPath!!)
        whImg.setCachePath(cachePath!!)
        updateStorageUsage()
    }

    fun settings():Settings=settings
    fun settingsLoaded():Boolean {
        return if (!settings.isLoaded()) {
            settings.load()
            false
        } else {
            true
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
        fun getById(idx:Int):ImageInfo {
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
                UpdateParts.THUMB_INFO -> setById(idx,getById(idx).copy(
                        thumbStatus = img.thumbStatus,
                    ))
                UpdateParts.IMAGE_INFO -> {
                    setById(idx,getById(idx).copy(
                        imageStatus=img.imageStatus,
                    ))
                }
                UpdateParts.EXTENDED_INFO -> {
                    setById(idx,getById(idx).copy(
                        extendedInfoStatus = img.extendedInfoStatus,
                        tags = img.tags,
                    ))
                }
                UpdateParts.FAVORITE_INFO -> {
                    setById(idx,getById(idx).copy(
                        inFavorites = img.inFavorites,
                    ))
                }
            }
            loadImage(getById(idx).copy(), WHFileType.THUMBNAIL)
            selectedImage?.value?.let {
                if (it.index == idx) {
                    selectedImage.value=getById(idx).copy()
                }
            }
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
                it?.let { (idx, status) ->
                    //Log.d(tag,"page $idx updated to status $status")
                    pagesData[idx] = status
                }
            }
        })
        jobs.add(coroutineScope!!.launch {
            suggester.collect() {
                //Log.d(tag,"Added ${it} to suggestion list")
                tagSuggestion.value=it
            }
        })
        jobs.add(coroutineScope!!.launch {
            _favoritesData.collect() {
                _favoritesData.value=it
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

    fun updateStorageUsage() {
        storeUsage.value= mapOf(
            StoreDataTypes.CACHE to getCachedDiskSpace(),
            StoreDataTypes.FAVORITES to getDataDiskSpace(),
            StoreDataTypes.FREE to getFreeDiskSpace(),
        )
    }

    fun clearStorage() {
        clear()
        whImg.clearCache()
        updateStorageUsage()
    }

    fun getTotalDiskSpace():Long {
        val statFs = StatFs(Environment.getRootDirectory().absolutePath);
        return statFs.blockCountLong * statFs.blockSizeLong
    }
    private fun getFreeDiskSpace():Long {
        val statFs = StatFs(Environment.getDataDirectory().absolutePath);
        return statFs.freeBlocksLong * statFs.blockSizeLong
    }

    private fun getCachedDiskSpace():Long {
        return whImg.getCacheSize()
    }
    private fun getDataDiskSpace():Long {
        return  favorites.getDataSize()
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
        } catch (_:Exception) {}
        return "0"
    }

    private fun isPageLoaded(page:Int):Boolean {
        return (pagesData.containsKey(page) && (
                (pagesData[page]==WHPageStatus.LOADED) || (pagesData[page] ==WHPageStatus.FAILED))
                )
    }
    fun loadImageInfo(id: Int) {
        if (!imageInfoLoaded(id)) {
            loadPage(imagePage(id))
        }
    }

    private fun loadPage(page:Int=0) {
        if (isPageLoaded(page)) {
            return
        }
        if (!pagesData.containsKey(page)) {
            pagesData[page]=WHPageStatus.NONE
        }
        if (pagesData[page]==WHPageStatus.LOADING) {
            return
        }
        pagesData[page]=WHPageStatus.LOADING
        threadPool.execute {
            loadPageT(page)
        }
    }

    private fun loadPageT(page:Int=0) {
        coroutineScope!!.launch {
            Log.d(tag,"loading page info $page")
            val s= WHSearch()
            val list = s.search(currentRequestData.copy().apply {
                this.page=page+1
            })
            if (list == null ) {
                pageInfo.emit(Pair(page,WHPageStatus.FAILED))
                return@launch
            }
            Log.d(tag,"page $page fetched")
            setTotal(list.meta.total)
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
                    val(thumbWidth,thumbHeight)= WHGetThumbDimentions(dimension_x,dimension_y, byWidth = true)
                    imageInfo.emit(
                        Triple(idx,
                            ImageInfo(
                                index=0,
                                id = id,
                                imagePath = path,
                                thumbPath = thumbs.original,
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
            pageInfo.emit(Pair(page,WHPageStatus.LOADED))
        }
    }

    private fun setTotal(total: Int) {
        imagesTotal.intValue = total

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
        clear()
    }

    private fun imageInfoLoaded(id:Int):Boolean {
        return imagesData.containsKey(id)
    }

    fun imageFromCache(id:String,type: WHFileType):String {
        return whImg.fromCache(id,type).absolutePath
    }

    fun imageFromFavorite(id:String,type: WHFileType):String {
        return favorites.fromData(id,type).absolutePath
    }

    private fun imageInCache(id: String,type: WHFileType):Boolean {
        return whImg.inCache(id,type)
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
                        var tries= IMAGE_LOAD_TRIES
                        while (whImg.toCache(img.id, type, getPath()).isEmpty()) {
                            Log.d(tag,"Image ${img.index} try ${tries}")
                            tries--
                            if (tries == 0) {break}
                            delay(Random.nextInt(IMAGE_LOAD_MIN_DELAY_MS, IMAGE_LOAD_MAX_DELAY_MS).toLong())
                        }
                        v = if (whImg.inCache(img.id,type)) {
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

    fun clearFilters(preserve: List<PossibleFilters> =emptyList<PossibleFilters>()) {
        if (!preserve.contains(PossibleFilters.CATEGORY)) {
            settings.whCatehory.value = WHCategories.GENERAL
        }
        if (!preserve.contains(PossibleFilters.RATIO)) {
            settings.whRatio.value = WHRatio.ANY
        }
        if (!preserve.contains(PossibleFilters.RESOLUTION)) {
            settings.whResolutionWidth.value = 0
            settings.whResolutionHeight.value = 0
        }
        if (!preserve.contains(PossibleFilters.COLOR)) {
            settings.whColor.value = ""
        }
        if (!preserve.contains(PossibleFilters.PURITY)) {
            settings.whPurity.value = WHPurity.SFW
        }
        if (!preserve.contains(PossibleFilters.TAGS)) {
            settings.whTags.value = emptyList()
        }
        if (!preserve.contains(PossibleFilters.IMAGE)) {
            fromImageString.value = ""
        }
        if (!preserve.contains(PossibleFilters.SEARCH)) {
            searchString.value = ""
        }
    }

    fun loadFromColor(color: WHColor) {
        clearFilters(listOf(PossibleFilters.PURITY))
        settings.whColor.value=color.name
        refresh()
    }

    fun loadFromTag(tag: WHTag) {
        clearFilters(listOf(PossibleFilters.PURITY))
        settings.whTags.value= mutableListOf(tag.tag)
        refresh()
    }

    fun loadFromImage(image: ImageInfo) {
        clearFilters(listOf(PossibleFilters.PURITY))
        fromImageString.value=image.id
        refresh()
    }

    fun addToFavorites(img:ImageInfo) {
        coroutineScope!!.launch {
            if (favorites.add(img)) {
                imageInfo.emit(
                    Triple(img.index, img.copy(inFavorites = true), UpdateParts.FAVORITE_INFO)
                )
            }
        }
    }
    private fun inFavorites(img:ImageInfo) {
        coroutineScope!!.launch {
            val rc=favorites.exists(img.id)
            imageInfo.emit(
                Triple(img.index,img.copy(inFavorites = rc),UpdateParts.FAVORITE_INFO)
            )
        }
    }

    fun removeFromFavorites(img:ImageInfo) {
        coroutineScope!!.launch {
            favorites.remove(img)
            imageInfo.emit(
                Triple(img.index, img.copy(inFavorites = false),UpdateParts.FAVORITE_INFO)
            )
        }
    }

    private fun clear() {
        setTotal(-1)
        imagesData.clear()
        pagesData.clear()
    }

    fun favoritesList() {
        coroutineScope!!.launch {
            val list=favorites.fetch().mapIndexed{i,v->
                v.copy(
                    imagePath = favorites.getDataFileAbsPath(v.id,WHFileType.IMAGE),
                    thumbPath = favorites.getDataFileAbsPath(v.id,WHFileType.THUMBNAIL),
                    index = i,
                )
            }
            _favoritesData.emit(list)
        }
    }

    fun clearFavorites() {
        coroutineScope!!.launch {
            favorites.removeAll()
            favoritesList()
        }
    }

    fun selectImage(idx: Int) {
        imageMode=ImageMode.SEARCH
        Log.d(tag,"selected image is $idx")
        imagesData[idx]?.let {
            selectedImage.value=it
            loadImage(it.copy(),WHFileType.IMAGE,)
            inFavorites(it.copy())
            loadExtendedInfo(it.copy())

            if (it.index>0) {
                previousImage.value=imagesData[it.index - 1]
                previousImage.value?.let {pi ->
                    loadPage(imagePage(pi.index))
                }
            } else {
                previousImage.value=null
            }
            if (it.index < (imagesTotal.value-1)) {
                nextImage.value=imagesData[it.index + 1]
                nextImage.value?.let {ni ->
                    loadPage(imagePage(ni.index))
                }
            } else {
                nextImage.value=null
            }
        }
    }

    fun selectFavouriteImage(image: ImageInfo) {
        imageMode=ImageMode.FAVORITES
        image.inFavorites=true
        selectedImage.value=image
        if (image.index>0) {
            previousImage.value=_favoritesData.value[image.index-1]
        } else {
            previousImage.value=null
        }
        if (image.index<(favoritesData.value.size-1)) {
            nextImage.value=_favoritesData.value[image.index+1]
        } else {
            nextImage.value=null
        }
    }

    fun toPreviousImage() {
        previousImage.value?.let {
            when (imageMode) {
                ImageMode.SEARCH ->selectImage(it.index)
                ImageMode.FAVORITES ->selectFavouriteImage(it)
            }
        }
    }

    fun toNextImage() {
        nextImage.value?.let {
            when (imageMode) {
                ImageMode.SEARCH ->selectImage(it.index)
                ImageMode.FAVORITES ->selectFavouriteImage(it)
            }
        }
    }


}

