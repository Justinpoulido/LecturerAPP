package com.example.lecturerapp.network

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.lecturerapp.utils.SocketCommunication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WifiDirectManager(private val context: Context, private val lifecycleScope: LifecycleCoroutineScope) {

    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    var availablePeers: List<WifiP2pDevice> = emptyList()
    private var isGroupOwner = false
    private var groupOwnerAddress: String? = null

    fun setupWiFiDirect() {
        wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager.initialize(context, context.mainLooper, null)

        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        context.registerReceiver(wifiDirectReceiver, intentFilter)

        discoverPeers()
    }

    private val wifiDirectReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> handleWifiStateChanged(intent)
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> requestPeers()
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION ->
                    wifiP2pManager.requestConnectionInfo(channel, connectionInfoListener)
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION ->
                    Toast.makeText(context, "Device details changed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleWifiStateChanged(intent: Intent) {
        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            Toast.makeText(context, "WiFi Direct is enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "WiFi Direct is not enabled", Toast.LENGTH_SHORT).show()
        }
    }

    // Updated method with permission check
    private fun discoverPeers() {
        if (!hasRequiredPermissions()) {
            Toast.makeText(context, "Location permission is required to discover peers.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Toast.makeText(context, "Peer discovery started", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reasonCode: Int) {
                    Toast.makeText(context, "Peer discovery failed: $reasonCode", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission denied for discovering peers.", Toast.LENGTH_SHORT).show()
        }
    }

    // Updated method with permission check
    private fun requestPeers() {
        if (!hasRequiredPermissions()) {
            Toast.makeText(context, "Location permission is required to request peers.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            wifiP2pManager.requestPeers(channel) { peerList ->
                availablePeers = peerList.deviceList.toList()
                Toast.makeText(context, "Peers discovered: ${availablePeers.size}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission denied for requesting peers.", Toast.LENGTH_SHORT).show()
        }
    }

    // Updated method with permission check
    fun connectToPeer(device: WifiP2pDevice) {
        if (!hasRequiredPermissions()) {
            Toast.makeText(context, "Location permission is required to connect to peers.", Toast.LENGTH_SHORT).show()
            return
        }

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        try {
            wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Toast.makeText(context, "Connecting to ${device.deviceName}", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Toast.makeText(context, "Connection failed: $reason", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission denied for connecting to peer.", Toast.LENGTH_SHORT).show()
        }
    }

    private val connectionInfoListener = WifiP2pManager.ConnectionInfoListener { info ->
        if (info.groupFormed && info.isGroupOwner) {
            isGroupOwner = true
            SocketCommunication.startServer(lifecycleScope)
        } else if (info.groupFormed) {
            groupOwnerAddress = info.groupOwnerAddress.hostAddress
            SocketCommunication.connectToServer(lifecycleScope, groupOwnerAddress!!)
        }
    }

    fun cleanup() {
        context.unregisterReceiver(wifiDirectReceiver)
    }

    // Helper function to check permissions
    private fun hasRequiredPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
    }
}

