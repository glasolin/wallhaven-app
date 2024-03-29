package otus.gpb.homework.wallhaven

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
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
    fun provideUiData()=UiData()
}

@Module
@InstallIn(SingletonComponent::class)
object UiStateModule {
    @Provides
    @Singleton
    fun provideUiState()=UiState()
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val data: UiData,
    private val state: UiState,
) : ViewModel() {
    private val context = MutableLiveData<Context>()
    private val coroutineScope = MutableLiveData<CoroutineScope>()

    init {
        context.observeForever { context ->
            // Do something with context
            //settings.setContext(context)
            state.setContext(context)
            data.setContext(context)
        }
        coroutineScope.observeForever { scope ->
            data.setCoroutineScope(scope)
        }
        //settings.load()
        data.load()
    }
    fun setContext(context: Context) {
        this.context.value = context
    }
    fun setCoroutineScope(scope: CoroutineScope) {
        coroutineScope.value=scope
    }

    fun data():UiData=data
    fun state():UiState=state
}

