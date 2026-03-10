package fr.isen.guillaume.disneyplusplus

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MovieListScreen(
    universeId: String?,
    onMovieClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Films de l'univers ID : \n$universeId")
        Spacer(modifier = Modifier.height(16.dp))

        // Simulation d'un clic sur un film spécifique (ex: La Menace Fantôme)
        val mockMovieId = "6c4c669a-413d-4566-ba1e-5a1ae527db39"
        Button(onClick = { onMovieClick(mockMovieId) }) {
            Text("Voir les détails du film test")
        }
    }
}