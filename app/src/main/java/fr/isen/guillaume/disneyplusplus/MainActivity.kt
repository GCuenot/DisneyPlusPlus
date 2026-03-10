package fr.isen.guillaume.disneyplusplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import fr.isen.guillaume.disneyplusplus.ui.theme.DisneyPlusPlusTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DisneyPlusPlusTheme {
                val navController = rememberNavController()
                val auth = remember { FirebaseAuth.getInstance() }

                // État de connexion initialisé selon l'utilisateur Firebase actuel
                var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(text = "Disney PlusPlus") },
                            navigationIcon = {
                                if (isLoggedIn && currentRoute != "universes" && currentRoute != "login") {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
                                    }
                                }
                            },
                            actions = {
                                if (isLoggedIn && currentRoute != "profile") {
                                    IconButton(onClick = { navController.navigate("profile") }) {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = "Aller au Profil")
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (auth.currentUser != null) "universes" else "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    isLoggedIn = true
                                    navController.navigate("universes") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("universes") {
                            UniverseListScreen(
                                onUniverseClick = { universeId ->
                                    navController.navigate("movies/$universeId")
                                }
                            )
                        }

                        composable("movies/{universeId}") { backStackEntry ->
                            val universeId = backStackEntry.arguments?.getString("universeId")
                            MovieListScreen(
                                universeId = universeId,
                                onMovieClick = { movieId ->
                                    navController.navigate("movie_detail/$movieId")
                                }
                            )
                        }

                        composable("movie_detail/{movieId}") { backStackEntry ->
                            val movieId = backStackEntry.arguments?.getString("movieId")
                            MovieDetailScreen(movieId = movieId)
                        }

                        composable("profile") {
                            ProfileScreen(
                                onLogout = {
                                    isLoggedIn = false
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}