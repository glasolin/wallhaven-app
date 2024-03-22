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
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

data class UiData(
    var todo:String="todo"
)

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {
    @Provides
    @Singleton
    fun provideSettings()=Settings()
}
@Module
@InstallIn(SingletonComponent::class)
object UiDataModule {
    @Provides
    @Singleton
    fun provideUiData()=UiData()
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val settings:Settings,
    private val data: UiData,
    private val state: UiState = UiState(),
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
    fun data():UiData=data
    fun state():UiState=state
}

