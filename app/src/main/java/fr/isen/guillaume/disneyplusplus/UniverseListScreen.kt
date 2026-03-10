package fr.isen.guillaume.disneyplusplus

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UniverseListScreen(onUniverseClick: (String) -> Unit) {
    // Fausses données basées sur ton JSON
    val mockUniverses = mapOf(
        "87261308-0ae0-4e42-9821-6e3eaa78c2bf" to "Star Wars",
        "a719a7b9-a3b3-413c-94ca-942fb8a2eafe" to "Marvel Cinematic Universe",
        "d82703fe-06c7-419b-9bb0-96d83b58f538" to "Avatar"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Choisis un Univers :", modifier = Modifier.padding(bottom = 16.dp))

        mockUniverses.forEach { (id, name) ->
            Button(
                onClick = { onUniverseClick(id) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(name)
            }
        }
    }
}