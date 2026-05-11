package fr.isen.guillaume.disneyplusplus.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// On crée notre schéma de couleurs "Cinéma"
private val DisneyColorScheme = darkColorScheme(
    primary = DisneyBlue,             // Boutons, interrupteurs, curseurs
    background = DisneyDarkBg,        // Le fond global de l'application
    surface = DisneyCardBg,           // Le fond des cartes, des champs de texte
    surfaceVariant = DisneyCardBg,    // Idem
    onPrimary = DisneyText,           // Le texte écrit sur les boutons primaires
    onBackground = DisneyText,        // Le texte général
    onSurface = DisneyText            // Le texte sur les cartes
)

@Composable
fun DisneyPlusPlusTheme(
    // On enlève les paramètres par défaut pour forcer notre style partout
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // On colorie la barre tout en haut du téléphone (où il y a l'heure et la batterie)
            window.statusBarColor = DisneyColorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = DisneyColorScheme,
        typography = Typography,
        content = content
    )
}