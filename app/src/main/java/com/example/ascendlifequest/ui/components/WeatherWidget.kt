package com.example.ascendlifequest.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume
import org.json.JSONObject

@Composable
fun WeatherWidget(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var temperature by remember { mutableStateOf<String?>(null) }
    var condition by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        try {
            loading = true
            error = false
            val loc = requestRealLocation(context)
            if (loc != null) {
                val weather = fetchWeather(loc.latitude, loc.longitude)
                if (weather != null) {
                    temperature = weather.first
                    condition = weather.second
                    code = weather.third
                } else {
                    error = true
                }
            } else {
                error = true
            }
        } catch (_: Exception) {
            error = true
        } finally {
            loading = false
        }
    }

    // Par défaut on affiche un rectangle aplati (largeur > hauteur)
    Box(
        modifier = Modifier
            .width(80.dp) // réduit encore
            .height(36.dp)
            .then(modifier)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 1.8.dp
                )
            }
            error -> {
                // Affichage compact en cas d'erreur
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "--°C",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "N/A",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            else -> {
                // Map weather code to icon
                val icon = when (code) {
                    0 -> Icons.Default.WbSunny
                    1, 2, 3 -> Icons.Default.WbSunny
                    45, 48 -> Icons.Default.BlurOn
                    51, 53, 55 -> Icons.Default.Opacity
                    61, 63, 65 -> Icons.Default.Opacity
                    71, 73, 75 -> Icons.Default.AcUnit
                    80, 81, 82 -> Icons.Default.Opacity
                    else -> Icons.AutoMirrored.Filled.HelpOutline
                }

                // Affichage aplati : icône à gauche, température centrée (texte plus grand)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = condition ?: "Météo",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = temperature ?: "--°C",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
suspend fun requestRealLocation(context: Context): Location? = withContext(Dispatchers.IO) {
    try {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val loc = withTimeoutOrNull(5000L) {
            suspendCancellableCoroutine<Location?> { cont ->
                try {
                    val task = client.lastLocation
                    task.addOnSuccessListener { location -> cont.resume(location) }
                    task.addOnFailureListener { cont.resume(null) }
                } catch (_: Exception) {
                    cont.resume(null)
                }
            }
        }
        return@withContext loc
    } catch (_: Exception) {
        return@withContext null
    }
}

suspend fun fetchWeather(lat: Double, lon: Double): Triple<String, String, Int>? = withContext(Dispatchers.IO) {
    try {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true&temperature_unit=celsius"
        val json = URL(url).readText()
        val obj = JSONObject(json)
        val current = obj.getJSONObject("current_weather")
        val temp = current.getDouble("temperature")
        val weatherCode = current.getInt("weathercode")
        val condition = mapWeatherCodeToText(weatherCode)
        return@withContext Triple(String.format(Locale.US, "%.0f°C", temp), condition, weatherCode)
    } catch (_: Exception) {
        return@withContext null
    }
}

fun mapWeatherCodeToText(code: Int): String {
    return when (code) {
        0 -> "Dégagé"
        1, 2, 3 -> "Partiellement nuageux"
        45, 48 -> "Brouillard"
        51, 53, 55 -> "Bruine"
        61, 63, 65 -> "Pluie"
        71, 73, 75 -> "Neige"
        80, 81, 82 -> "Averses"
        else -> "Inconnu"
    }
}
