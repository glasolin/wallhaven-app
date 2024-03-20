package otus.gpb.homework.wallhaven

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.widget.ThemedSpinnerAdapter
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import otus.gpb.homework.wallhaven.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainActivityViewModel>()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        viewModel.setContext(applicationContext)
        splashScreen.setKeepOnScreenCondition {
            !viewModel.settingsLoaded()
        }
        enableEdgeToEdge()
        setContent {
            val darkTheme = shouldUseDarkTheme(viewModel.settings())

            // Update the edge to edge configuration to match the theme
            // This is the same parameters as the default enableEdgeToEdge call, but we manually
            // resolve whether or not to show dark theme using uiState, since it can be different
            // than the configuration's dark theme value based on the user preference.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim,
                        darkScrim,
                    ) { darkTheme },
                )
                onDispose {}
            }
            viewModel.state().windowSizeClass=calculateWindowSizeClass(this)
            viewModel.state().coroutineScope=rememberCoroutineScope()



            CompositionLocalProvider(
            ) {
                AppTheme(
                    darkTheme = darkTheme,
                    //androidTheme = shouldUseAndroidTheme(uiState),
                    //disableDynamicTheming = shouldDisableDynamicTheming(uiState),
                ) {
                    App(viewModel.state())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}
@Composable
private fun shouldUseDarkTheme(settings: Settings):Boolean =
    when (settings.theme) {
        Themes.SYSTEM -> isSystemInDarkTheme()
        Themes.LIGHT -> false
        Themes.DARK -> true
    }


private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
