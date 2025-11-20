package com.example.ascendlifequest.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
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

private const val TAG = "WeatherWidget"

@Composable
fun WeatherWidget() {
    val context = LocalContext.current
    var temperature by remember { mutableStateOf<String?>(null) }
    var condition by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // request location and fetch weather when entering the Quest screen
        loading = true
        val loc = requestRealLocation(context)
        if (loc != null) {
            val weather = fetchWeather(loc.latitude, loc.longitude)
            temperature = weather?.first
            condition = weather?.second
        }
        loading = false
    }

    Box(
        modifier = Modifier
            .width(84.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = temperature ?: "--°C", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = condition ?: "--", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@SuppressLint("MissingPermission")
suspend fun requestRealLocation(context: Context): Location? = withContext(Dispatchers.IO) {
    try {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val task = client.lastLocation
        val loc = suspendCancellableCoroutine<Location?> { cont ->
            task.addOnSuccessListener { location ->
                cont.resume(location)
            }
            task.addOnFailureListener { ex ->
                cont.resume(null)
            }
        }
        return@withContext loc
    } catch (ex: Exception) {
        Log.e(TAG, "Erreur localisation", ex)
        return@withContext null
    }
}

suspend fun fetchWeather(lat: Double, lon: Double): Pair<String, String>? = withContext(Dispatchers.IO) {
    try {
        // Open-Meteo API: no API key required
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true&temperature_unit=celsius"
        val json = URL(url).readText()
        val obj = JSONObject(json)
        val current = obj.getJSONObject("current_weather")
        val temp = current.getDouble("temperature")
        val weatherCode = current.getInt("weathercode")
        val condition = mapWeatherCodeToText(weatherCode)
        return@withContext String.format(Locale.US, "%.0f°C", temp) to condition
    } catch (ex: Exception) {
        Log.e(TAG, "Erreur fetchWeather", ex)
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
