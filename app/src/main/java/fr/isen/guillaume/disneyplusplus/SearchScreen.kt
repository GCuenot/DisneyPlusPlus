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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (showUniverses && searchQuery.isNotEmpty()) {
                    val filtered = allUniverses.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    if (filtered.isNotEmpty()) {
                        item { Text("Univers", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 8.dp)) }
                        items(filtered) { univ ->
                            ListItem(
                                headlineContent = { Text(univ.name) },
                                modifier = Modifier.clickable { onUniverseClick(univ.id) },
                                overlineContent = { Text("UNIVERS") }
                            )
                        }
                    }
                }

                if (showSagas && searchQuery.isNotEmpty()) {
                    val filtered = allSagas.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    if (filtered.isNotEmpty()) {
                        item { Text("Sagas", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 8.dp)) }
                        items(filtered) { saga ->
                            ListItem(
                                headlineContent = { Text(saga.name) },
                                modifier = Modifier.clickable { onUniverseClick(saga.universe_id) },
                                overlineContent = { Text("SAGA") }
                            )
                        }
                    }
                }

                if (showMovies && searchQuery.isNotEmpty()) {
                    val filtered = allMovies.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    if (filtered.isNotEmpty()) {
                        item { Text("Films", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 8.dp)) }
                        items(filtered) { movie ->
                            ListItem(
                                headlineContent = { Text(movie.title) },
                                modifier = Modifier.clickable { onMovieClick(movie.id) },
                                supportingContent = { Text(movie.release_date) },
                                overlineContent = { Text("FILM") }
                            )
                        }
                    }
                }
            }
        }
    }
}
