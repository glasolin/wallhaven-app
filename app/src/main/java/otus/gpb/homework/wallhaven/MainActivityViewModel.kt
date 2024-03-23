package otus.gpb.homework.wallhaven

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import otus.gpb.homework.wallhaven.ui.UiData
import otus.gpb.homework.wallhaven.ui.UiState
import javax.inject.Inject
import javax.inject.Singleton

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
    fun provideUiData()= UiData()
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val settings:Settings,
    private val data: UiData,
    private val state: UiState = UiState(),
) : ViewModel() {
    private val context = MutableLiveData<Context>()

    init {
        context.observeForever { context ->
            // Do something with context
            settings.setContext(context)
            state.setContext(context)
            data.setContext(context)
        }
        settings.load()
    }
    fun setContext(context: Context) {
        this.context.value = context
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
    fun state(): UiState =state
}

