package com.sandymist.android.devicemonitor.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Suppress("unused")
class NetworkMonitor private constructor(context: Context) {
    private var lastNetworkEventTimestamp = 0L
    private val scope = CoroutineScope(Dispatchers.IO)
    private val isInAirplaneModeNow = Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    private val airplaneModeFlow = MutableStateFlow(isInAirplaneModeNow)
    private val airplaneModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            scope.launch {
                val isAirplaneModeOn = intent?.getBooleanExtra("state", false) ?: false
                airplaneModeFlow.emit(isAirplaneModeOn)
            }
        }
    }
    private val callback: ConnectivityManager. NetworkCallback

    private val _networkStatus = MutableStateFlow<INetworkStatus>(
        NetworkStatus.Unknown(
            lastNetworkEventTimestamp,
            airplaneModeFlow.value,
        )
    )
    @OptIn(FlowPreview::class)
    val networkStatus = _networkStatus
        .debounce(1000L)
        .map {
            lastNetworkEventTimestamp = System.currentTimeMillis()
            it
        }
        .stateIn(
            initialValue = NetworkStatus.Unknown(lastNetworkEventTimestamp, airplaneModeFlow.value),
            scope = scope,
            started = WhileSubscribed(5000L),
        )

    private val connectivityManager by lazy {
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE,
        ) as ConnectivityManager
    }

    init {
        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network: Connected")
                val availableConnectionStatus = getNetworkConnectionStatus(network)
                val activeConnectionStatus = connectivityManager.activeNetwork?.let { getNetworkConnectionStatus(it) }
                scope.launch {
                    val status = NetworkStatus.Connected(
                        availableConnectionStatus,
                        activeConnectionStatus,
                        lastNetworkEventTimestamp,
                        airplaneModeFlow.value,
                    )
                    Log.d(TAG, "Network connected status: $status")
                    _networkStatus.emit(status)
                }
            }

            override fun onUnavailable() {
                Log.e(TAG, "Network: Unavailable")
                val activeConnectionStatus = connectivityManager.activeNetwork?.let { getNetworkConnectionStatus(it) }
                scope.launch {
                    val status = NetworkStatus.Disconnected(
                        activeConnectionStatus,
                        lastNetworkEventTimestamp,
                        airplaneModeFlow.value,
                    )
                    Log.d(TAG, "Network unavailable status: $status")
                    _networkStatus.emit(status)
                }
            }

            override fun onLost(network: Network) {
                Log.e(TAG, "Network: Disconnected")
                val activeConnectionStatus = connectivityManager.activeNetwork?.let { getNetworkConnectionStatus(it) }
                scope.launch {
                    val status = NetworkStatus.Disconnected(
                        activeConnectionStatus,
                        lastNetworkEventTimestamp,
                        airplaneModeFlow.value,
                    )
                    Log.d(TAG, "Network disconnected status: $status")
                    _networkStatus.emit(status)
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        Log.d(TAG, "Network: register listener")
        connectivityManager.registerNetworkCallback(request, callback)
        emitCurrentNetworkStatus()

        // airplane mode monitoring
        scope.launch {
            airplaneModeFlow.collectLatest {
                emitCurrentNetworkStatus()
            }
        }

        val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        context.registerReceiver(airplaneModeReceiver, filter)
    }

    private fun emitCurrentNetworkStatus() {
        connectivityManager.activeNetwork?.let {
            val activeConnectionStatus = connectivityManager.activeNetwork?.let { getNetworkConnectionStatus(it) }
            scope.launch {
                _networkStatus.emit(
                    NetworkStatus.Connected(
                        null,
                        activeConnectionStatus,
                        lastNetworkEventTimestamp,
                        airplaneModeFlow.value,
                    )
                )
            }
        } ?: run {
            scope.launch {
                val activeConnectionStatus = connectivityManager.activeNetwork?.let { getNetworkConnectionStatus(it) }
                _networkStatus.emit(
                    NetworkStatus.Disconnected(
                        activeConnectionStatus,
                        lastNetworkEventTimestamp,
                        airplaneModeFlow.value,
                    )
                )
            }
        }
    }

    private fun getNetworkConnectionStatus(network: Network): ConnectionStatus? {
        return connectivityManager.getNetworkCapabilities(network)?.let {
            val connectionStatus = ConnectionStatus(
                usingWiFi = it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
                usingCellular = it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
                usingVPN = it.hasTransport(NetworkCapabilities.TRANSPORT_VPN),
                isValidated = it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
                isBehindCaptivePortal = it.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL),
                isNotMetered = it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
            )
            return connectionStatus
        }
    }

    fun shutdown(context: Context) {
        connectivityManager.unregisterNetworkCallback(callback)
        context.unregisterReceiver(airplaneModeReceiver)
    }

    companion object {
        private const val TAG = "NetworkMonitor"

        @Volatile
        private var INSTANCE: NetworkMonitor? = null

        fun getInstance(context: Context): NetworkMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkMonitor(context).also { INSTANCE = it }
            }
        }
    }
}
