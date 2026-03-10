package fr.isen.guillaume.disneyplusplus

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    // 1. LE CERVEAU : On crée nos variables d'état
    var isLoginMode by remember { mutableStateOf(true) } // Vrai = Connexion, Faux = Inscription
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .animateContentSize(), // Ajoute une petite animation fluide quand l'écran change de taille
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Le Titre / Logo
        Text(
            text = "Disney PlusPlus",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 2. LE SÉLECTEUR : Connexion ou Inscription
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
                    .clickable { isLoginMode = true }
                    .padding(8.dp)
            )
            Text(
                text = "S'inscrire",
                fontSize = 18.sp,
                fontWeight = if (!isLoginMode) FontWeight.Bold else FontWeight.Normal,
                color = if (!isLoginMode) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier
                    .clickable { isLoginMode = false }
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. LES CHAMPS DE SAISIE
        // Champ Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Adresse e-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // Ouvre le clavier avec le "@"
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ Mot de passe
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            // Cache le mot de passe si isPasswordVisible est faux
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                // L'icône de l'œil à la fin du champ
                val image = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = "Afficher le mot de passe")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 4. LE BOUTON D'ACTION DYNAMIQUE
        Button(
            onClick = {
                // TODO : Ici on mettra la logique Firebase plus tard (signInWithEmailAndPassword ou createUserWithEmailAndPassword)
                // Pour l'instant, on simule un succès direct :
                onLoginSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            // Le texte du bouton change tout seul selon le mode !
            Text(
                text = if (isLoginMode) "SE CONNECTER" else "CRÉER MON COMPTE",
                fontSize = 16.sp
            )
        }
    }
}