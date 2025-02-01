package com.sandymist.android.devicemonitor

import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandymist.android.devicemonitor.audio.AudioMonitor
import com.sandymist.android.devicemonitor.audio.AudioStatus
import com.sandymist.android.devicemonitor.network.NetworkMonitor
import com.sandymist.android.devicemonitor.network.NetworkStatus
import com.sandymist.android.devicemonitor.power.PowerMonitor
import com.sandymist.android.devicemonitor.power.PowerStatus
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonitorScreen(
    modifier: Modifier = Modifier,
) {
    // TODO: Inject these
    val context = LocalContext.current
    val networkMonitor = NetworkMonitor.getInstance(context)
    val powerMonitor = PowerMonitor.getInstance(context)
    val audioMonitor = AudioMonitor.getInstance(context)
    val deviceMonitor = DeviceMonitor(context)

    val networkStatus by networkMonitor.networkStatus.collectAsStateWithLifecycle()
    val powerStatus by powerMonitor.powerStatus.collectAsStateWithLifecycle()
    val audioStatus by audioMonitor.audioStatus.collectAsStateWithLifecycle()
    val deviceMonitorString by deviceMonitor.deviceMonitorFlow.collectAsStateWithLifecycle(
        initialValue = "",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 30.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Device Monitor",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
        Timber.d("Data: $deviceMonitorString")
        Text(
            text = deviceMonitorString,
            style = MaterialTheme.typography.bodyLarge,
        )

        HorizontalDivider()

        Text(
            text = "Network status",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = networkStatus.statusName(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )

        val activeStatus = when (networkStatus) {
            is NetworkStatus.Connected -> (networkStatus as NetworkStatus.Connected).activeConnectionStatus
            is NetworkStatus.Disconnected -> (networkStatus as NetworkStatus.Disconnected).activeConnectionStatus
            else -> null
        }

        activeStatus?.let {
            StatusRow("WiFi", it.usingWiFi.toString())
            StatusRow("Cellular", it.usingCellular.toString())
            StatusRow("VPN", it.usingVPN.toString())
            StatusRow("Validated", it.isValidated.toString())
            StatusRow("Captive", it.isBehindCaptivePortal.toString())
            StatusRow("Unmetered", it.isNotMetered.toString())
        }

        StatusRow("Since", networkStatus.since.convertMillisToDateTime())
        val span = DateUtils.getRelativeTimeSpanString(networkStatus.since)
        StatusRow("Duration", span.toString())

        HorizontalDivider()

        Text(
            text = "Power status",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
        when (powerStatus) {
            is PowerStatus.Unknown -> {
                Text(
                    text = "Unknown",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            is PowerStatus.Available -> {
                StatusRow("Battery saver", (powerStatus as PowerStatus.Available).isPowerSaveMode.toString())
                StatusRow("Idle mode", (powerStatus as PowerStatus.Available).isDeviceIdleMode.toString())
            }
        }

        HorizontalDivider()

        Text(
            text = "Audio status",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
        Timber.d("Audio: $audioStatus")

        when (audioStatus) {
            is AudioStatus.Unknown -> {
                Text(
                    text = "Unknown",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            is AudioStatus.Available -> {
                StatusRow("Device", (audioStatus as AudioStatus.Available).device)
            }
        }
    }
}

@Composable
fun StatusRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = key, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Long.convertMillisToDateTime(): String {
    if (this == 0L) return "Launch"
    val instant = Instant.ofEpochMilli(this)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
