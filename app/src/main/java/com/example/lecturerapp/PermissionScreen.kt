package com.example.lecturerapp.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lecturerapp.utils.PermissionsManager

// Permission Screen Composable
@Composable
fun PermissionScreen(onGoToSettingsClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "No Permissions",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "To use the attendance and feedback app, we require that all permissions are granted.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
            )
            Button(onClick = { onGoToSettingsClick() }) {
                Text(text = "Go to Settings")
            }
        }
    }
}

@Composable
fun PermissionRequestScreen() {
    val context = LocalContext.current
    val permissionsManager = PermissionsManager(context)

    PermissionScreen(
        onGoToSettingsClick = {
            permissionsManager.openAppSettings() // Navigate to the settings
        }
    )
}
