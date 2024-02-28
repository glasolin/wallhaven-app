package otus.gpb.homework.wallhaven.ui.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.fragment
import otus.gpb.homework.wallhaven.R

class NavigationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            // Dispose the Composition when viewLifecycleOwner is destroyed
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )
            setContent {
                MaterialTheme {
                    UiNavigation( )
                }
            }
        }
    }

    // Define the Profile composable.
    @Composable
    fun Home(onNavigateToHome: () -> Unit) {
        Text("Home")
        Button(onClick = { onNavigateToHome() }) {
            Text("Go to Home")
        }
    }

    // Define the FriendsList composable.
    @Composable
    fun Favorites(onNavigateToFavorites: () -> Unit) {
        Text("Favorites")
        Button(onClick = { onNavigateToFavorites() }) {
            Text("Go to Favorites")
        }
    }
    @Composable
    fun Settings(onNavigateToSettings: () -> Unit) {
        Text("Settings")
        Button(onClick = { onNavigateToSettings() }) {
            Text("Go to Favorites")
        }
    }


    @Composable
    @Preview
    fun UiNavigation( ) {
        val navController =  findNavController(R.id.nav_fragment)

        navController.graph = navController.createGraph(
            startDestination = "home"
        ) {
            // Associate each destination with one of the route constants.
            fragment<HomeFragment>("home") {
                label = "Home"
            }

            fragment<FavoritesFragment>("favorites") {
                label = "Favorites"
            }

            fragment<SettingsFragment>("settings") {
                label = "Settings"
            }

            // Add other fragment destinations similarly.
        }
        NavHost(navController, startDestination = "profile") {
            composable("home") { Home(onNavigateToHome = { navController.navigate("home") }) }
            composable("favorites") { Favorites(onNavigateToFavorites = { navController.navigate("favorites") }) }
            composable("settings") { Settings(onNavigateToSettings = { navController.navigate("settings") }) }
        }

        /*Column(
             Modifier.fillMaxWidth()
                 .padding(top = 20.dp, bottom = 20.dp)
                 .background(Color.Yellow)
         ) {
             Button(
                 onClick = { }
             ) {
                 Text("open contacts", Modifier.fillMaxWidth())
             }
             Button(onClick = {  }) {
                 Text("open cart")
             }
             Button(onClick = {  }) {
                 Text("sign in")
             }
         }*/

    }
}