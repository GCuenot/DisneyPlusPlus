package fr.isen.guillaume.disneyplusplus

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MovieDetailScreen(movieId: String?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Détails du film ID : $movieId")
        Spacer(modifier = Modifier.height(32.dp))

        // Placeholders pour les fonctionnalités futures
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { /* TODO Firebase */ }) { Text("Vu") }
            Button(onClick = { /* TODO Firebase */ }) { Text("À voir") }
        }
    }
}