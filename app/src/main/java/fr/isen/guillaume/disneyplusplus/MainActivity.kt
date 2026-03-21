package fr.isen.guillaume.disneyplusplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

                var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 1. Notre fameux dégradé subtil (du plus clair au plus sombre)
                val backgroundBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E2A3A), // Haut-Gauche : Un bleu-nuit légèrement plus clair
                        Color(0xFF040714)  // Bas-Droite : Le noir/bleu très profond du thème
                    )
                    // Par défaut, linearGradient va du coin en haut à gauche vers le bas à droite !
                )

                // 2. On englobe tout dans une Box qui porte le dégradé
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundBrush)
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent, // <-- IMPORTANT : Le Scaffold devient transparent !
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground, // <-- AJOUTE CETTE LIGNE MAGIQUE,
                        topBar = {
                            @OptIn(ExperimentalMaterial3Api::class)
                            TopAppBar(
                                title = { Text(text = "Disney PlusPlus") },
                                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent // <-- La barre du haut aussi !
                                ),
                                navigationIcon = {
                                    if (isLoggedIn && currentRoute != "universes" && currentRoute != "login") {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
                                        }
                                    }
                                },
                                actions = {
                                    if (isLoggedIn) {
                                        if (currentRoute != "search") {
                                            IconButton(onClick = { navController.navigate("search") }) {
                                                Icon(imageVector = Icons.Default.Search, contentDescription = "Rechercher")
                                            }
                                        }
                                        if (currentRoute != "profile") {
                                            IconButton(onClick = { navController.navigate("profile") }) {
                                                Icon(imageVector = Icons.Default.Person, contentDescription = "Profil")
                                            }
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
                            // ... Tes routes composable("...") habituelles ne changent pas !
                            // Laisse tout le contenu de ton NavHost exactement comme il est.

                            // (Je mets juste le début pour que tu voies où ça s'insère)
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

                            // --> COPIE ICI LE RESTE DE TES COMPOSABLES <--
                            composable("universes") { UniverseListScreen(onUniverseClick = { navController.navigate("movies/$it") }) }
                            composable("movies/{universeId}") { MovieListScreen(universeId = it.arguments?.getString("universeId"), onMovieClick = { id -> navController.navigate("movie_detail/$id") }) }
                            composable("movie_detail/{movieId}") { MovieDetailScreen(movieId = it.arguments?.getString("movieId")) }
                            composable("profile") { ProfileScreen(onLogout = { isLoggedIn = false; navController.navigate("login") { popUpTo(0) { inclusive = true } } }) }
                            composable("search") { SearchScreen(onMovieClick = { navController.navigate("movie_detail/$it") }, onUniverseClick = { navController.navigate("movies/$it") }) }
                        }
                    }
                }
            }
        }
    }
}