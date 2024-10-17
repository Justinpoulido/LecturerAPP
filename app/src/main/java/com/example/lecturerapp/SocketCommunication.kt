package com.example.lecturerapp.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import androidx.lifecycle.LifecycleCoroutineScope

object SocketCommunication {

    fun startServer(scope: LifecycleCoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(8888)
            while (!serverSocket.isClosed) {
                val client = serverSocket.accept()
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                val writer = BufferedWriter(OutputStreamWriter(client.getOutputStream()))

                val message = reader.readLine()
                val reversedMessage = message.reversed()
                writer.write("$reversedMessage\n")
                writer.flush()

                reader.close()
                writer.close()
                client.close()
            }
        }
    }

    fun connectToServer(scope: LifecycleCoroutineScope, groupOwnerAddress: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val socket = Socket(groupOwnerAddress, 8888)
                val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                writer.write("Hello from client\n")
                writer.flush()

                val response = reader.readLine()
                withContext(Dispatchers.Main) {
                    println("Response from server: $response")
                }

                writer.close()
                reader.close()
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
