package com.sandymist.android.devicemonitor.demo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.sandymist.android.devicemonitor.DeviceMonitor
import com.sandymist.android.devicemonitor.MonitorScreen
import com.sandymist.android.devicemonitor.ui.theme.DeviceMonitorTheme
import com.sandymist.android.devicemonitor.audio.AudioMonitor
import com.sandymist.android.devicemonitor.network.NetworkMonitor
import com.sandymist.android.devicemonitor.power.PowerMonitor

class MainActivity : ComponentActivity() {
    private val networkMonitor by lazy { NetworkMonitor.getInstance(this) }
    private val powerMonitor by lazy { PowerMonitor.getInstance(this) }
    private val audioMonitor by lazy { AudioMonitor.getInstance(this) }
    private val deviceMonitor by lazy { DeviceMonitor(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            DeviceMonitorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        MonitorScreen(
                            modifier = Modifier.padding(innerPadding),
                            networkMonitor = networkMonitor,
                            powerMonitor = powerMonitor,
                            audioMonitor = audioMonitor,
                            deviceMonitor = deviceMonitor,
                        )
                    } else {
                        Text("Not supported")
                    }
                }
            }
        }
    }
}
