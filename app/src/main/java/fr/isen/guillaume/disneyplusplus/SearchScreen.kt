package fr.isen.guillaume.disneyplusplus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onMovieClick: (String) -> Unit,
    onUniverseClick: (String) -> Unit
) {
    val database = FirebaseDatabase.getInstance("https://disneyplusplus-2a2a9-default-rtdb.europe-west1.firebasedatabase.app/").reference
    
    var searchQuery by remember { mutableStateOf("") }
    var showMovies by remember { mutableStateOf(true) }
    var showUniverses by remember { mutableStateOf(true) }
    var showSagas by remember { mutableStateOf(true) }
    
    var allMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var allUniverses by remember { mutableStateOf<List<Universe>>(emptyList()) }
    var allSagas by remember { mutableStateOf<List<Saga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val moviesList = mutableListOf<Movie>()
                snapshot.child("movies").children.forEach { child ->
                    child.getValue(Movie::class.java)?.copy(id = child.key ?: "")?.let { moviesList.add(it) }
                }
                allMovies = moviesList

                val univList = mutableListOf<Universe>()
                snapshot.child("universes").children.forEach { child ->
                    child.getValue(Universe::class.java)?.copy(id = child.key ?: "")?.let { univList.add(it) }
                }
                allUniverses = univList

                val sagasList = mutableListOf<Saga>()
                snapshot.child("sagas").children.forEach { child ->
                    child.getValue(Saga::class.java)?.copy(id = child.key ?: "")?.let { sagasList.add(it) }
                }
                allSagas = sagasList
                
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher un film, univers...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = showMovies, onClick = { showMovies = !showMovies }, label = { Text("Films") })
            FilterChip(selected = showUniverses, onClick = { showUniverses = !showUniverses }, label = { Text("Univers") })
            FilterChip(selected = showSagas, onClick = { showSagas = !showSagas }, label = { Text("Sagas") })
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
                // 1. On prépare nos listes filtrées
                val filteredUniverses = if (showUniverses && searchQuery.isNotEmpty()) allUniverses.filter { it.name.contains(searchQuery, ignoreCase = true) } else emptyList()
                val filteredSagas = if (showSagas && searchQuery.isNotEmpty()) allSagas.filter { it.name.contains(searchQuery, ignoreCase = true) } else emptyList()
                val filteredMovies = if (showMovies && searchQuery.isNotEmpty()) allMovies.filter { it.title.contains(searchQuery, ignoreCase = true) } else emptyList()

                // 2. On vérifie si TOUT est vide (c'est notre Empty State !)
                val isSearchEmpty = searchQuery.isNotEmpty() && filteredUniverses.isEmpty() && filteredSagas.isEmpty() && filteredMovies.isEmpty()

                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    // L'AFFICHAGE DE L'EMPTY STATE
                    if (isSearchEmpty) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Oups !",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aucun résultat pour \"$searchQuery\".\nEssayez avec d'autres mots-clés.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // L'AFFICHAGE DES RÉSULTATS (S'il y en a)
                    if (filteredUniverses.isNotEmpty()) {
                        item { Text("Univers", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 8.dp)) }
                        items(filteredUniverses) { univ ->
                            ListItem(
                                headlineContent = { Text(univ.name) },
                                modifier = Modifier.clickable { onUniverseClick(univ.id) },
                                overlineContent = { Text("UNIVERS", color = MaterialTheme.colorScheme.primary) }
                            )
                        }
                    }

                    if (filteredSagas.isNotEmpty()) {
                        item { Text("Sagas", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 8.dp)) }
                        items(filteredSagas) { saga ->
                            ListItem(
                                headlineContent = { Text(saga.name) },
                                modifier = Modifier.clickable { onUniverseClick(saga.universe_id) },
                                overlineContent = { Text("SAGA", color = MaterialTheme.colorScheme.primary) }
                            )
                        }
                    }

                    if (filteredMovies.isNotEmpty()) {
                        item { Text("Films", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 8.dp)) }
                        items(filteredMovies) { movie ->
                            ListItem(
                                headlineContent = { Text(movie.title) },
                                modifier = Modifier.clickable { onMovieClick(movie.id) },
                                supportingContent = { Text(movie.release_date) },
                                overlineContent = { Text("FILM", color = MaterialTheme.colorScheme.primary) }
                            )
                        }
                    }
                }
        }
    }
}
