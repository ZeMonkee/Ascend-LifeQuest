package com.example.ascendlifequest.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume
import org.json.JSONObject

@Composable
fun WeatherWidget() {
    val context = LocalContext.current
    var temperature by remember { mutableStateOf<String?>(null) }
    var condition by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

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
                } else {
                    error = true
                }
            } else {
                error = true
            }
        } catch (ex: Exception) {
            error = true
        } finally {
            loading = false
        }
    }

    Box(
        modifier = Modifier
            .width(120.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.5.dp
                )
            }
            error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "--°C",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = temperature ?: "--°C",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = condition ?: "--",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
        val loc = kotlinx.coroutines.withTimeoutOrNull(5000L) {
            suspendCancellableCoroutine<Location?> { cont ->
                try {
                    val task = client.lastLocation
                    task.addOnSuccessListener { location -> cont.resume(location) }
                    task.addOnFailureListener { cont.resume(null) }
                } catch (ex: Exception) {
                    cont.resume(null)
                }
            }
        }
        return@withContext loc
    } catch (ex: Exception) {
        return@withContext null
    }
}

suspend fun fetchWeather(lat: Double, lon: Double): Pair<String, String>? = withContext(Dispatchers.IO) {
    try {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true&temperature_unit=celsius"
        val json = URL(url).readText()
        val obj = JSONObject(json)
        val current = obj.getJSONObject("current_weather")
        val temp = current.getDouble("temperature")
        val weatherCode = current.getInt("weathercode")
        val condition = mapWeatherCodeToText(weatherCode)
        return@withContext String.format(Locale.US, "%.0f°C", temp) to condition
    } catch (ex: Exception) {
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
