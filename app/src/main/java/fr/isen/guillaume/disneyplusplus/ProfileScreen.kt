package fr.isen.guillaume.disneyplusplus

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    // On précise la même URL que dans LoginScreen pour la région Europe
    val database = FirebaseDatabase.getInstance("https://disneyplusplus-2a2a9-default-rtdb.europe-west1.firebasedatabase.app/").reference
    val context = LocalContext.current
    
    var firstName by remember { mutableStateOf("Chargement...") }
    var lastName by remember { mutableStateOf("") }
    
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            database.child("users").child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    firstName = snapshot.child("firstName").value?.toString() ?: "Prénom inconnu"
                    lastName = snapshot.child("lastName").value?.toString() ?: ""
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Erreur de chargement: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val creationDate = user?.metadata?.creationTimestamp?.let { sdf.format(Date(it)) } ?: "Inconnue"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.size(100.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (firstName == "Chargement...") firstName else "$firstName $lastName",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        ProfileInfoCard(
            icon = Icons.Default.Email,
            label = "Adresse e-mail",
            value = user?.email ?: "Non connecté"
        )

        Spacer(modifier = Modifier.height(12.dp))
        
        ProfileInfoCard(
            icon = Icons.Default.CalendarToday,
            label = "Membre depuis le",
            value = creationDate
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                user?.email?.let { email ->
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "E-mail de réinitialisation envoyé !", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Erreur : ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.LockReset, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("RÉINITIALISER LE MOT DE PASSE")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                auth.signOut()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SE DÉCONNECTER", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileInfoCard(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
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
