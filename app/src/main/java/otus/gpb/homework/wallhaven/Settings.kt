package otus.gpb.homework.wallhaven

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

@Serializable
data class SettingsData (
    var theme:Themes = Themes.SYSTEM,
)

class Settings {
    var status:SettingsStatus=SettingsStatus.NONE
        private set
    private val tag = "Settings"
    private val preferencesTag= stringPreferencesKey("JSON_user_prefs")
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings.dat")
    private var preferences = SettingsData()
    private var context:Context?=null
    var theme=preferences.theme
        set(value) {
            field=value
            store()
         }
        get() {
            return preferences.theme
        }

    fun store() {
        if (context == null) {
            Log.d(tag, "context not set")
            return
        }
        if (status != SettingsStatus.READY) {
            status = SettingsStatus.PENDING
        } else {
            status = SettingsStatus.STORING
            val json = Json.encodeToString(preferences)
            val ctx=context
            runBlocking {
                launch {
                    ctx!!.dataStore.edit { settings ->
                        settings[preferencesTag] = json
                    }
                    if (status == SettingsStatus.PENDING) {
                        status = SettingsStatus.READY
                        store()
                    } else {
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
        if ((status!=SettingsStatus.READY) && (status!=SettingsStatus.NONE)) {
            //ignore non ready for load statuses
            return;
        }
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

    fun setContext(context: Context) {
        this.context=context
    }
}