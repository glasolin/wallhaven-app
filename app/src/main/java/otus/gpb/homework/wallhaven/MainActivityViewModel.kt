package otus.gpb.homework.wallhaven

import android.service.autofill.UserData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

interface UserDataRepository {
    val userData: Flow<UserData>
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: UserDataRepository,
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = repository.userData.map {
        MainActivityUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )
}


sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val userData: UserData) : MainActivityUiState
}
