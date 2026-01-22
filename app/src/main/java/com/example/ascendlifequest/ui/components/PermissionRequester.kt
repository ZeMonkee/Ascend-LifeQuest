package com.example.ascendlifequest.ui.components

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

/**
 * Requests location permission on first app launch. Shows system permission dialog and persists the
 * request state.
 */
@Composable
fun PermissionRequester() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val firstLaunch = remember { prefs.getBoolean("first_launch", true) }

    val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted
                ->
                prefs.edit { putBoolean("first_launch", false) }
                prefs.edit { putBoolean("first_launch", false) }
            }

    LaunchedEffect(firstLaunch) {
        if (firstLaunch) {
            // request permission immediately on first app open
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
