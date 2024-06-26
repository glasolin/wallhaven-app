package otus.gpb.homework.wallhaven

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import otus.gpb.homework.wallhaven.wh.WHCategories
import otus.gpb.homework.wallhaven.wh.WHColors
import otus.gpb.homework.wallhaven.wh.WHOrder
import otus.gpb.homework.wallhaven.wh.WHPurity
import otus.gpb.homework.wallhaven.wh.WHRatio
import otus.gpb.homework.wallhaven.wh.WHSorting
import javax.inject.Inject



enum class Themes {
    SYSTEM, LIGHT, DARK
}

enum class SettingsStatus {
    NONE, READY, LOADING, STORING, PENDING
}

@Serializable
data class SettingsData (
    var theme:Themes = Themes.SYSTEM,
    var apiKey:String = "",
    var sorting: WHSorting =WHSorting.DATE_ADDED,
    var order: WHOrder=WHOrder.DESC,
    var whCategory: WHCategories = WHCategories.GENERAL,
    var whPurity: WHPurity = WHPurity.SFW,
    var whRatio: WHRatio = WHRatio.ANY,
    var whResolutionWidth:Int = 0,
    var whResolutionHeight:Int = 0,
    var whColor:String="",
    var whTags: List<String> = emptyList()
)

class Settings {
    private var status:SettingsStatus=SettingsStatus.NONE
    private val tag = "Settings"
    private val preferencesTag= stringPreferencesKey("JSON_user_prefs")
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings.dat")
    private var preferences = SettingsData()
    private var context:Context?=null
    private var oldJson=""

    var theme = storeObserver<Themes>(preferences.theme,"theme") {v->
        preferences.theme=v
    }
    var sorting = storeObserver<WHSorting>(preferences.sorting,"sorting") {v->
        preferences.sorting=v
    }
    var order = storeObserver<WHOrder>(preferences.order,"sortingDesc") {v->
        preferences.order=v
    }
    var apiKey = storeObserver<String>(preferences.apiKey,"apiKey") {v->
        preferences.apiKey=v
    }

    var whCatehory = storeObserver<WHCategories>(preferences.whCategory,"whCategory") {v->
        preferences.whCategory=v
    }
    var whPurity = storeObserver<WHPurity>(preferences.whPurity,"whPurity") {v->
        preferences.whPurity=v
    }
    var whRatio = storeObserver<WHRatio>(preferences.whRatio,"whRatio") {v->
        preferences.whRatio=v
    }

    var whResolutionWidth= storeObserver<Int>(preferences.whResolutionWidth,"whResolutionWidth") {v->
        preferences.whResolutionWidth=v
    }
    var whResolutionHeight= storeObserver<Int>(preferences.whResolutionHeight,"whResolutionWidth") {v->
        preferences.whResolutionHeight=v
    }
    var whColor= storeObserver<String>(preferences.whColor,"whColor") {v->
        preferences.whColor=v
    }
    var whTags= storeObserver<List<String>>(preferences.whTags,"whTags") {v->
        preferences.whTags=v
    }

    private fun applySettings() {
        Log.d(tag, "applying preferences...")
        theme.value=preferences.theme
        apiKey.value=preferences.apiKey
        sorting.value=preferences.sorting
        order.value=preferences.order
        whCatehory.value=preferences.whCategory
        whPurity.value=preferences.whPurity
        whRatio.value=preferences.whRatio
        whResolutionWidth.value=preferences.whResolutionWidth
        whResolutionHeight.value=preferences.whResolutionHeight
        whColor.value=preferences.whColor
        whTags.value=preferences.whTags
        Log.d(tag, "applying done")
    }

    private fun <T> storeObserver(data:T,description:String="", onChange:(v:T) -> Unit):MutableLiveData<T> {
        return MutableLiveData<T>(data).apply {
            observeForever {
                if (description.isNotEmpty()) {
                    Log.d(tag, "changed ${description} to ${it}")
                } else {
                    Log.d(tag, "changed to ${it}")
                }
                onChange(it)
                if (isLoaded()) {
                    Log.d(tag, "is loaded, calling smart store")
                    smartStore()
                }
            }
        }
    }

    private fun smartStore() {
        val newJson = Json.encodeToString(preferences)
        if (newJson!=oldJson) {
            oldJson=newJson
            store()
        }
    }
    private fun store() {
        if (context == null) {
            Log.d(tag, "context not set")
            return
        }
        if (status != SettingsStatus.READY) {
            Log.d(tag, "store pending...")
            status = SettingsStatus.PENDING
        } else {
            Log.d(tag, "storing...")
            status = SettingsStatus.STORING
            val json = Json.encodeToString(preferences)
            Log.d(tag, json)
            val ctx=context
            runBlocking {
                launch {
                    try {
                        ctx!!.dataStore.edit { settings ->
                            settings[preferencesTag] = json
                        }
                        if (status == SettingsStatus.PENDING) {
                            status = SettingsStatus.READY
                            store()
                        } else {
                            status = SettingsStatus.READY
                            Log.d(tag, "stored")
                        }
                    } catch (e: Exception) {
                        Log.d(tag, e.message.toString())
                    } finally {
                        status = SettingsStatus.READY
                    }
                }
            }
        }
    }

    fun isLoading():Boolean=(status==SettingsStatus.LOADING)
    fun isStoring():Boolean=(status==SettingsStatus.STORING)
    fun isReady():Boolean=(status==SettingsStatus.READY)
    fun isBusy():Boolean=((status==SettingsStatus.LOADING) ||
            (status==SettingsStatus.STORING) ||
            (status==SettingsStatus.PENDING))

    fun isLoaded():Boolean=((status!=SettingsStatus.NONE) && (status!=SettingsStatus.LOADING))

    fun load() {
        if (context == null) {
            Log.d(tag, "context not set")
            return
        }
        if (status!=SettingsStatus.NONE) {
            //ignore non ready for load statuses
            Log.d(tag, "already loaded")
            return;
        }
        Log.d(tag, "loading...")
        status = SettingsStatus.LOADING
        val ctx=context
        runBlocking {
            launch {
                val json: Flow<String> = ctx!!.dataStore.data
                    .map { settings ->
                        // No type safety.
                        settings[preferencesTag] ?: ""
                    }
                json.firstOrNull().also {
                    try {
                        if (it != null) {
                            preferences = Json.decodeFromString<SettingsData>(it)
                            applySettings()
                        }
                    } catch (e: Exception) {
                        Log.d(tag, "loading failed")
                        Log.d(tag, e.message.toString())
                    } finally {
                        Log.d(tag, "loaded")
                        status = SettingsStatus.READY
                    }
                }
            }
        }
    }

    fun setContext(context: Context) {
        this.context=context
    }
}