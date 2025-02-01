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
import com.sandymist.android.devicemonitor.MonitorScreen
import com.sandymist.android.devicemonitor.ui.theme.DeviceMonitorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            DeviceMonitorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        MonitorScreen(
                            modifier = Modifier.padding(innerPadding),
                        )
                    } else {
                        Text("Not supported")
                    }
                }
            }
        }
    }
}
