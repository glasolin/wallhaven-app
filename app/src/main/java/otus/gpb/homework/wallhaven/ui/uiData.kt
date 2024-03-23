package otus.gpb.homework.wallhaven.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import otus.gpb.homework.wallhaven.Themes

enum class storeDataTypes {
    NONE,FAVORITES, CACHE, FREE
}

class UiData constructor() {
    private var context: Context?=null
    var storeUsage = mutableStateOf<Map<storeDataTypes,Int>>((emptyMap ()))

    init {
        updateStorageUsage()
    }
    fun setContext(context: Context) {
        this.context=context
    }

    fun updateStorageUsage() {
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
            storeDataTypes.FREE to 1000,
        )
    }
}