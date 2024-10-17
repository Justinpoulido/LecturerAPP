package com.example.lecturerapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity

class PermissionsManager(private val context: Context) {

    // Launches the app's settings screen
    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // Function to handle denied permissions (e.g., showing a toast or custom message)
    fun handleDeniedPermissions() {
        Toast.makeText(
            context,
            "Permissions are necessary for the app to function. Please grant them in settings.",
            Toast.LENGTH_LONG
        ).show()
    }
}
