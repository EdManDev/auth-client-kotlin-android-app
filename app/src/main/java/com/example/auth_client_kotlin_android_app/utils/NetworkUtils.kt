package com.example.auth_client_kotlin_android_app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

object NetworkUtils {

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    suspend fun testServerConnectivity(serverUrl: String): NetworkTestResult {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url(serverUrl)
                    .head() // Use HEAD request for connectivity test
                    .build()

                val response = client.newCall(request).execute()
                response.close()

                NetworkTestResult.Success("Server reachable at $serverUrl")

            } catch (e: IOException) {
                NetworkTestResult.Error("Cannot reach server: ${e.message}")
            } catch (e: Exception) {
                NetworkTestResult.Error("Network test failed: ${e.message}")
            }
        }
    }

    suspend fun testPortConnectivity(host: String, port: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), 5000)
                    true
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}

sealed class NetworkTestResult {
    data class Success(val message: String) : NetworkTestResult()
    data class Error(val message: String) : NetworkTestResult()
}