package otus.gpb.homework.wallhaven

import android.app.Application
import android.content.Context
import android.service.autofill.UserData
import androidx.datastore.preferences.core.preferencesOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.Provides
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class uiData(
    var todo:String="todo"
)
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val settings: Settings = Settings(),
    private val data: uiData = uiData(),
    private val state: uiState = uiState(),
) : ViewModel() {
    private val contextLiveData = MutableLiveData<Context>()
    init {
        contextLiveData.observeForever { context ->
            // Do something with context
            settings.setContext(context)
        }
        settings.load()
    }
    fun setContext(context: Context) {
        contextLiveData.value = context
    }

    fun settingsLoaded():Boolean {
        if (!settings.isLoaded()) {
            settings.load()
            return false
        } else {
            return true
        }
    }

    fun settings():Settings=settings
    fun data():uiData=data
    fun state():uiState=state
}

