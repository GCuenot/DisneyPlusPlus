package fr.isen.guillaume.disneyplusplus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
        ) {
            AsyncImage(
                model = movie.image_url,
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Année : ${movie.release_date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
