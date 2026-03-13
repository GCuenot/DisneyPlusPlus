package fr.isen.guillaume.disneyplusplus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Movie(
    val id: String = "",
    val title: String = "",
    val universe_id: String = "",
    val saga_id: String? = null,
    val release_date: String = "",
    val image_url: String = ""
)

data class Saga(
    val id: String = "",
    val name: String = "",
    val universe_id: String = ""
)

@Composable
fun MovieListScreen(
    universeId: String?,
    onMovieClick: (String) -> Unit
) {
    val database = FirebaseDatabase.getInstance("https://disneyplusplus-2a2a9-default-rtdb.europe-west1.firebasedatabase.app/").reference
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var sagas by remember { mutableStateOf<List<Saga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(universeId) {
        if (universeId == null) return@LaunchedEffect
        
        // Charger les sagas pour cet univers
        database.child("sagas").orderByChild("universe_id").equalTo(universeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Saga>()
                    snapshot.children.forEach { child ->
                        val saga = child.getValue(Saga::class.java)?.copy(id = child.key ?: "")
                        if (saga != null) list.add(saga)
                    }
                    sagas = list
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // Charger TOUS les films et filtrer manuellement (plus fiable si l'indexation Firebase tarde)
        database.child("movies").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Movie>()
                snapshot.children.forEach { child ->
                    val m = child.getValue(Movie::class.java)?.copy(id = child.key ?: "")
                    if (m?.universe_id == universeId) {
                        list.add(m)
                    }
                }
                movies = list
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Groupement par Sagas
            sagas.forEach { saga ->
                val sagaMovies = movies.filter { it.saga_id == saga.id }
                if (sagaMovies.isNotEmpty()) {
                    item {
                        Text(
                            text = saga.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(sagaMovies) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
                    }
                }
            }

            // Films sans saga (ou si aucune saga n'existe dans cet univers)
            val independentMovies = movies.filter { m -> 
                m.saga_id == null || m.saga_id == "" || sagas.none { it.id == m.saga_id }
            }
            
            if (independentMovies.isNotEmpty()) {
                if (sagas.isNotEmpty()) {
                    item {
                        Text(
                            text = "Autres films",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                items(independentMovies) { movie ->
                    MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
                }
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = movie.image_url,
                contentDescription = movie.title,
                modifier = Modifier
                    .width(70.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Année : ${movie.release_date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
