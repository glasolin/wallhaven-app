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
import javax.inject.Inject



enum class Themes {
    SYSTEM, LIGHT, DARK
}

enum class SettingsStatus {
    NONE, READY, LOADING, STORING, PENDING
}

enum class Sorting {
    DATE_ADDED, RELEVANCE, RANDOM, VIEWS, FAVORITES, TOPLIST
}

@Serializable
data class SettingsData (
    var theme:Themes = Themes.SYSTEM,
    var apiKey:String = "",
    var sorting:Sorting=Sorting.DATE_ADDED,
    var sortingDesc:Boolean=true,
)

class Settings {
    private var status:SettingsStatus=SettingsStatus.NONE
    private val tag = "Settings"
    private val preferencesTag= stringPreferencesKey("JSON_user_prefs")
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings.dat")
    private var preferences = SettingsData()
    private var context:Context?=null

     var theme = storeObserver<Themes>(preferences.theme,"theme") {v->
        preferences.theme=v
    }
    var sorting = storeObserver<Sorting>(preferences.sorting,"sorting") {v->
        preferences.sorting=v
    }
    var sortingDesc = storeObserver<Boolean>(preferences.sortingDesc,"sortingDesc") {v->
        preferences.sortingDesc=v
    }
    var apiKey = storeObserver<String>(preferences.apiKey,"apiKey") {v->
        preferences.apiKey=v
    }

    private fun applySettings() {
        Log.d(tag, "applying preferences...")
        theme.value=preferences.theme
        apiKey.value=preferences.apiKey
        sorting.value=preferences.sorting
        sortingDesc.value=preferences.sortingDesc
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
                    smartStore(data, it)
                }
            }
        }
    }

    private fun <T> smartStore(old:T,new:T ) {
        if (old!=new) {

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