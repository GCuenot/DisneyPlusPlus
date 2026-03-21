package fr.isen.guillaume.disneyplusplus

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance("https://disneyplusplus-2a2a9-default-rtdb.europe-west1.firebasedatabase.app/").reference

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Disney PlusPlus",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Se connecter",
                fontSize = 18.sp,
                fontWeight = if (isLoginMode) FontWeight.Bold else FontWeight.Normal,
                color = if (isLoginMode) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier
                    .clickable { isLoginMode = true; errorMessage = null }
                    .padding(8.dp)
            )
            Text(
                text = "S'inscrire",
                fontSize = 18.sp,
                fontWeight = if (!isLoginMode) FontWeight.Bold else FontWeight.Normal,
                color = if (!isLoginMode) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier
                    .clickable { isLoginMode = false; errorMessage = null }
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(visible = !isLoginMode) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Prénom") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Nom") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isLoading
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Adresse e-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Mot de passe") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = "Afficher le mot de passe")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        if (isLoginMode) {
            Text(
                text = "Mot de passe oublié ?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        if (email.isBlank()) {
                            errorMessage = "Veuillez entrer votre e-mail"
                        } else {
                            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "E-mail envoyé", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = task.exception?.localizedMessage
                                }
                            }
                        }
                    }
                    .padding(vertical = 8.dp)
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank() || (!isLoginMode && (firstName.isBlank() || lastName.isBlank()))) {
                    errorMessage = "Veuillez remplir tous les champs"
                    return@Button
                }

                isLoading = true
                if (isLoginMode) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                errorMessage = task.exception?.localizedMessage
                            }
                        }
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                if (userId != null) {
                                    val userMap = mapOf(
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                        "email" to email
                                    )
                                    database.child("users").child(userId).setValue(userMap)
                                        .addOnCompleteListener { dbTask ->
                                            isLoading = false
                                            if (dbTask.isSuccessful) {
                                                Toast.makeText(context, "Compte créé !", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            } else {
                                                errorMessage = "Erreur base de données : ${dbTask.exception?.localizedMessage}"
                                            }
                                        }
                                } else {
                                    isLoading = false
                                }
                            } else {
                                isLoading = false
                                errorMessage = task.exception?.localizedMessage
                            }
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(text = if (isLoginMode) "SE CONNECTER" else "S'INSCRIRE")
            }
        }
    }
}
