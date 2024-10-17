package com.example.lecturerapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.lecturerapp.ui.theme.LecturerAppTheme
import androidx.compose.foundation.BorderStroke

class ClassManagementActivity : ComponentActivity() {

    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver

    // MutableState to hold connected devices
    private val connectedDevicesState = mutableStateOf(listOf<String>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get data passed from StartClassActivity
        val wifiPassword = intent.getStringExtra("wifi_password") ?: ""

        // Initialize WifiP2pManager
        wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager.initialize(this, mainLooper, null)

        // Set up broadcast receiver for connection events
        receiver = WiFiDirectBroadcastReceiver(wifiP2pManager, channel, object : WiFiDirectBroadcastReceiver.WifiDirectCallback {
            override fun onDeviceConnected(device: WifiP2pDevice) {
                // Add connected device to the list
                connectedDevicesState.value = connectedDevicesState.value + device.deviceName
            }

            override fun onDeviceDisconnected(device: WifiP2pDevice) {
                // Remove disconnected device from the list
                connectedDevicesState.value = connectedDevicesState.value - device.deviceName
            }
        })

        // Register the receiver to listen for Wi-Fi Direct events
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        }
        registerReceiver(receiver, intentFilter)

        // Get the Wi-Fi network SSID
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val networkSSID = wifiInfo.ssid.removePrefix("\"").removeSuffix("\"") // Remove surrounding quotes

        // Set the Compose content
        setContent {
            LecturerAppTheme {
                ClassManagementScreen(
                    networkSSID = networkSSID,
                    wifiPassword = wifiPassword,
                    attendees = connectedDevicesState.value, // Pass connected devices state
                            onEndClass = {
                                // Call finish to end the activity and return to the previous screen
                                finish()
                   }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver) // Don't forget to unregister!
    }
}
@Composable
fun ClassManagementScreen(networkSSID: String,wifiPassword: String, attendees: List<String>,onEndClass: () -> Unit) {
    var message by remember { mutableStateOf(TextFieldValue("")) }
    var chatLog by remember { mutableStateOf(listOf<String>()) } // Chat log to display sent messages

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Class network information
        Text(
            text = "Class Network:$networkSSID: ",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        Text(text = "Network Password:$wifiPassword",
                    style = MaterialTheme . typography . titleLarge, modifier = Modifier.padding(8.dp))


        Spacer(modifier = Modifier.height(16.dp))

        // "End Class" Button
        Button(
            onClick = { onEndClass() }, // Call the passed function when clicked
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error // Set the background color to red
            )
        ) {
            Text(text = "End Class")
        }


        // Attendees list
        Text(text = "Attendees", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.fillMaxHeight(0.4f).padding(8.dp)) {
            items(attendees.size) { index ->
                Text(text = attendees[index], style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat log
        LazyColumn(modifier = Modifier.fillMaxHeight(0.4f).padding(8.dp)) {
            items(chatLog.size) { index ->
                Text(text = chatLog[index], style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Input field for questions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f).padding(8.dp),
                decorationBox = { innerTextField ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Box(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            if (message.text.isEmpty()) {
                                Text(
                                    text = "Ask a question...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    // Add question to the chat log and clear the input
                    if (message.text.isNotEmpty()) {
                        chatLog = chatLog + "You: ${message.text}"
                        message = TextFieldValue("")
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(text = "Send")
            }
        }
    }
}

