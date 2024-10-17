package com.example.lecturerapp.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import androidx.core.content.ContextCompat

class WiFiDirectBroadcastReceiver(
    private val wifiP2pManager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val callback: WifiDirectCallback
) : BroadcastReceiver() {

    interface WifiDirectCallback {
        fun onDeviceConnected(device: WifiP2pDevice)
        fun onDeviceDisconnected(device: WifiP2pDevice)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Check if the required permission is granted
                if (ContextCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission not granted, skip further actions or show a message
                    return
                }

                val networkInfo: NetworkInfo? =
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                if (networkInfo?.isConnected == true) {
                    // A device has connected, request connection info
                    try {
                        wifiP2pManager.requestConnectionInfo(channel) { info ->
                            if (info.groupFormed && info.isGroupOwner) {
                                // We are the group owner, request group info to get connected devices
                                wifiP2pManager.requestGroupInfo(channel) { group ->
                                    group.clientList.forEach { device ->
                                        callback.onDeviceConnected(device)
                                    }
                                }
                            }
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                        // Handle exception appropriately (e.g., show a message to the user)
                    }
                } else {
                    // A device has disconnected, request group info to update the list
                    try {
                        wifiP2pManager.requestGroupInfo(channel) { group ->
                            group.clientList.forEach { device ->
                                callback.onDeviceDisconnected(device)
                            }
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                        // Handle exception appropriately
                    }
                }
            }
        }
    }
}
