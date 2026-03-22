package fr.isen.guillaume.disneyplusplus

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val database = FirebaseDatabase.getInstance("https://disneyplusplus-2a2a9-default-rtdb.europe-west1.firebasedatabase.app/").reference
    val context = LocalContext.current
    
    var firstName by remember { mutableStateOf("Chargement...") }
    var lastName by remember { mutableStateOf("") }
    var ownedMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var userStatuses by remember { mutableStateOf<Map<String, Map<String, Boolean>>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            database.child("users").child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    firstName = snapshot.child("firstName").value?.toString() ?: "Prénom inconnu"
                    lastName = snapshot.child("lastName").value?.toString() ?: ""
                    
                    val statuses = mutableMapOf<String, Map<String, Boolean>>()
                    val movieIds = mutableListOf<String>()
                    
                    snapshot.child("movie_status").children.forEach { movieSnapshot ->
                        val movieId = movieSnapshot.key ?: ""
                        val statusMap = mutableMapOf<String, Boolean>()
                        movieSnapshot.children.forEach { statusChild ->
                            statusMap[statusChild.key ?: ""] = statusChild.getValue(Boolean::class.java) ?: false
                        }
                        statuses[movieId] = statusMap
                        
                        if (statusMap.values.any { it }) {
                            movieIds.add(movieId)
                        }
                    }
                    userStatuses = statuses
                    
                    if (movieIds.isNotEmpty()) {
                        database.child("movies").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(movieSnapshot: DataSnapshot) {
                                val list = mutableListOf<Movie>()
                                movieSnapshot.children.forEach { child ->
                                    if (movieIds.contains(child.key)) {
                                        val movie = child.getValue(Movie::class.java)?.copy(id = child.key ?: "")
                                        if (movie != null) list.add(movie)
                                    }
                                }
                                ownedMovies = list
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    } else {
                        ownedMovies = emptyList()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    val filteredMovies = ownedMovies.filter { 
        it.title.contains(searchQuery, ignoreCase = true) 
    }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val creationDate = user?.metadata?.creationTimestamp?.let { sdf.format(Date(it)) } ?: "Inconnue"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                modifier = Modifier.size(100.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "$firstName $lastName", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            
            ProfileInfoCard(icon = Icons.Default.Email, label = "Adresse e-mail", value = user?.email ?: "Non connecté")
            Spacer(modifier = Modifier.height(8.dp))
            ProfileInfoCard(icon = Icons.Default.CalendarToday, label = "Membre depuis le", value = creationDate)
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Ma Filmographie", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                placeholder = { Text("Rechercher dans mes films...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
        }

        if (filteredMovies.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (searchQuery.isEmpty()) Icons.Default.Movie else Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "Votre collection est vide" else "Aucun résultat",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isEmpty())
                            "Explorez les univers pour ajouter des films à votre filmographie."
                        else
                            "Nous n'avons pas trouvé de film pour \"$searchQuery\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            items(filteredMovies) { movie ->
                CollectionMovieCard(
                    movie = movie,
                    status = userStatuses[movie.id] ?: emptyMap(),
                    onStatusChange = { field, value ->
                        user?.uid?.let { uid ->
                            database.child("users").child(uid).child("movie_status").child(movie.id).child(field).setValue(value)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = {
                    user?.email?.let { email ->
                        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                            if (task.isSuccessful) Toast.makeText(context, "E-mail envoyé !", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LockReset, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RÉINITIALISER LE MOT DE PASSE")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { auth.signOut(); onLogout() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SE DÉCONNECTER", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileInfoCard(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CollectionMovieCard(
    movie: Movie,
    status: Map<String, Boolean>,
    onStatusChange: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
            .padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = movie.image_url,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = movie.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = movie.release_date, fontSize = 14.sp, color = Color.Gray)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    StatusSwitch(label = "Vu", checked = status["watched"] ?: false) { onStatusChange("watched", it) }
                }
                Box(modifier = Modifier.weight(1f)) {
                    StatusSwitch(label = "À voir", checked = status["wantToWatch"] ?: false) { onStatusChange("wantToWatch", it) }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    StatusSwitch(label = "Possédé", checked = status["own"] ?: false) { onStatusChange("own", it) }
                }
                Box(modifier = Modifier.weight(1f)) {
                    StatusSwitch(label = "Céder", checked = status["getRid"] ?: false, enabled = status["own"] ?: false) { onStatusChange("getRid", it) }
                }
            }
        }
    }
}


@Composable
fun StatusSwitch(label: String, checked: Boolean, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.scale(0.7f)
        )
    }
}