package com.example.ascendlifequest.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

private const val TAG = "WeatherWidget"

@Composable
fun WeatherWidget(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var temperature by remember { mutableStateOf<Double?>(null) }
    var description by remember { mutableStateOf<String?>(null) }
    var permissionGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Flag to ensure we request permission only once while the composable is active
    val requestedOnce = remember { mutableStateOf(false) }

    // Launcher to request permission interactively
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) {
            // Once granted, fetch location & weather
            fetchLocationAndWeather(fusedLocationClient) { lat, lon ->
                fetchWeather(lat, lon) { temp, desc ->
                    temperature = temp
                    description = desc
                }
            }
        }
    }

    // When the composable enters composition, if permission already granted, fetch;
    // otherwise request the permission automatically once (no button)
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            fetchLocationAndWeather(fusedLocationClient) { lat, lon ->
                fetchWeather(lat, lon) { temp, desc ->
                    temperature = temp
                    description = desc
                }
            }
        } else {
            if (!requestedOnce.value) {
                requestedOnce.value = true
                // Launch the system permission request dialog
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Card(
        modifier = modifier
            .wrapContentSize()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (temperature != null && description != null) {
                Text(text = "${temperature!!.toInt()}°C", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description!!, style = MaterialTheme.typography.bodySmall)
            } else {
                if (!permissionGranted) {
                    // We requested the permission automatically; show a note while waiting for user's decision
                    Text(text = "Demande d'autorisation en cours...", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(text = "Chargement...", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocationAndWeather(
    fusedLocationClient: FusedLocationProviderClient,
    onLocation: (lat: Double, lon: Double) -> Unit
) {
    Log.d(TAG, "Demande de la localisation actuelle…")
    val cancellationTokenSource = CancellationTokenSource()

    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocation(location.latitude, location.longitude)
            } else {
                Log.w(TAG, "Impossible d'obtenir la localisation (résultat nul).")
            }
        }
        .addOnFailureListener { exception ->
            Log.e(TAG, "Échec de la récupération de la localisation.", exception)
        }
}
