package com.example.lecturerapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.lecturerapp.network.WifiDirectManager
import com.example.lecturerapp.ui.AttendanceFeedbackScreen
import com.example.lecturerapp.ui.ClassManagementActivity
import com.example.lecturerapp.ui.PermissionRequestScreen
import com.example.lecturerapp.utils.PermissionsManager

class MainActivity : ComponentActivity() {

    private lateinit var permissionsManager: PermissionsManager
    private lateinit var wifiDirectManager: WifiDirectManager  // Declare WifiDirectManager

    private var allPermissionsGranted by mutableStateOf(false)
    private var showAttendanceScreen by mutableStateOf(false) // This controls screen state

    // Permissions to request
    private val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val wifiStateGranted = permissions[Manifest.permission.ACCESS_WIFI_STATE] ?: false
        val nearbyDevicesGranted = permissions[Manifest.permission.NEARBY_WIFI_DEVICES] ?: false

        allPermissionsGranted = fineLocationGranted && wifiStateGranted && nearbyDevicesGranted

        if (allPermissionsGranted) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            showAttendanceScreen = true  // Navigate to attendance screen
            wifiDirectManager.setupWiFiDirect() // Start Wi-Fi Direct once permissions are granted
        } else {
            permissionsManager.handleDeniedPermissions() // Ensure this handles permission denial properly
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionsManager = PermissionsManager(this)
        wifiDirectManager = WifiDirectManager(this, lifecycleScope) // Initialize WifiDirectManager

        requestPermissionsIfNeeded() // Move permission logic here

        setContent {
            MaterialTheme {
                // Switch between Permission screen and Attendance/Feedback screen based on permission state
                if (showAttendanceScreen) {
                    AttendanceFeedbackScreen(onStartClassClick = {
                        // Handle the Start Class button click
                        Toast.makeText(this, "Class Started!", Toast.LENGTH_SHORT).show()
                        // Add navigation or further logic here for starting a class
                        startClass() // This would be your new method or intent logic
                    })
                } else if (allPermissionsGranted) {
                    // Future logic for when permissions are granted
                } else {
                    // Show Permission Screen when permissions are not granted
                    PermissionRequestScreen()
                }
            }
        }
    }

    // Request necessary permissions if they are not already granted
    private fun requestPermissionsIfNeeded() {
        val permissionsNotGranted = permissionsToRequest.any { permission ->
            ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNotGranted) {
            // If permissions are not granted, request them
            permissionLauncher.launch(permissionsToRequest)
        } else {
            // If all permissions are already granted, proceed to show attendance screen
            allPermissionsGranted = true
            showAttendanceScreen = true
            wifiDirectManager.setupWiFiDirect() // Initialize Wi-Fi Direct
        }
    }

    private fun startClass() {
        // Create an Intent to launch the ClassManagementActivity
        val intent = Intent(this, ClassManagementActivity::class.java)

        // Optionally, pass extra data to the ClassManagementActivity
        intent.putExtra("course_name", "Intro to Android")  // Replace with actual data
        intent.putExtra("course_code", "AND101")            // Replace with actual data
        intent.putExtra("session_number", "1")              // Replace with actual data
        intent.putExtra("session_type", "Lecture")          // Replace with actual data

        // Get the connected peers (students) from WifiDirectManager and pass to ClassManagementActivity
        val connectedPeers = wifiDirectManager.availablePeers.map { it.deviceName }
        intent.putStringArrayListExtra("attendees", ArrayList(connectedPeers))

        // Start the activity
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup resources like unregistering WifiDirectManager receivers if needed
        wifiDirectManager.cleanup()
    }
}
