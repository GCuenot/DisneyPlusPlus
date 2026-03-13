package fr.isen.guillaume.disneyplusplus

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun MovieDetailScreen(movieId: String?) {
    val database = FirebaseDatabase.getInstance("https://disneyplusplus-2a2a9-default-rtdb.europe-west1.firebasedatabase.app/").reference
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val context = LocalContext.current

    var movie by remember { mutableStateOf<Movie?>(null) }
    var userStatus by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var ownersWithGettingRidOf by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(movieId) {
        if (movieId == null) return@LaunchedEffect

        // Charger les détails du film
        database.child("movies").child(movieId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                movie = snapshot.getValue(Movie::class.java)?.copy(id = snapshot.key ?: "")
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })

        // Charger le statut de l'utilisateur actuel pour ce film
        if (userId != null) {
            database.child("users").child(userId).child("movie_status").child(movieId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val status = mutableMapOf<String, Boolean>()
                        snapshot.children.forEach { child ->
                            status[child.key ?: ""] = child.getValue(Boolean::class.java) ?: false
                        }
                        userStatus = status
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        // Charger les autres utilisateurs qui veulent s'en débarrasser
        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<String>()
                snapshot.children.forEach { userSnapshot ->
                    val otherUserId = userSnapshot.key
                    if (otherUserId != userId) {
                        val hasIt = userSnapshot.child("movie_status").child(movieId).child("own").getValue(Boolean::class.java) ?: false
                        val wantsToGetRid = userSnapshot.child("movie_status").child(movieId).child("getRid").getValue(Boolean::class.java) ?: false
                        if (hasIt && wantsToGetRid) {
                            val name = userSnapshot.child("firstName").value.toString()
                            list.add(name)
                        }
                    }
                }
                ownersWithGettingRidOf = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else movie?.let { m ->
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                AsyncImage(
                    model = m.image_url,
                    contentDescription = m.title,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = m.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Sortie : ${m.release_date}", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Mon Statut", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    StatusRow(
                        label = "Vu",
                        isActive = userStatus["watched"] ?: false,
                        icon = Icons.Default.Visibility,
                        onClick = { toggleStatus(database, userId, movieId, "watched", !(userStatus["watched"] ?: false)) }
                    )
                    StatusRow(
                        label = "À voir",
                        isActive = userStatus["wantToWatch"] ?: false,
                        icon = Icons.Default.Bookmark,
                        onClick = { toggleStatus(database, userId, movieId, "wantToWatch", !(userStatus["wantToWatch"] ?: false)) }
                    )
                    StatusRow(
                        label = "Possédé (DVD/Blu-Ray)",
                        isActive = userStatus["own"] ?: false,
                        icon = Icons.Default.Album,
                        onClick = { toggleStatus(database, userId, movieId, "own", !(userStatus["own"] ?: false)) }
                    )
                    if (userStatus["own"] == true) {
                        StatusRow(
                            label = "Souhaite s'en débarrasser",
                            isActive = userStatus["getRid"] ?: false,
                            icon = Icons.Default.DeleteForever,
                            onClick = { toggleStatus(database, userId, movieId, "getRid", !(userStatus["getRid"] ?: false)) }
                        )
                    }

                    if (ownersWithGettingRidOf.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = "Disponibilité", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Ces utilisateurs possèdent ce film et souhaitent s'en débarrasser :", style = MaterialTheme.typography.bodySmall)
                                ownersWithGettingRidOf.forEach { name ->
                                    Text("• $name", fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusRow(label: String, isActive: Boolean, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = isActive, onCheckedChange = { onClick() })
    }
}

fun toggleStatus(database: com.google.firebase.database.DatabaseReference, userId: String?, movieId: String?, field: String, value: Boolean) {
    if (userId != null && movieId != null) {
        database.child("users").child(userId).child("movie_status").child(movieId).child(field).setValue(value)
    }
}
