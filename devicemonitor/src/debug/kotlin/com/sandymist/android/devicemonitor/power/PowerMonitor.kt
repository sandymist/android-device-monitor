package com.sandymist.android.devicemonitor.power

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PowerMonitor private constructor(context: Context) {
    private val _powerStatus = MutableStateFlow<PowerStatus>(PowerStatus.Unknown)
    val powerStatus = _powerStatus.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        context.emitPowerStatus(scope)

        val filter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                scope.launch {
                    context?.emitPowerStatus(scope)
                }
            }
        }, filter)
    }

    private fun Context.emitPowerStatus(scope: CoroutineScope) {
        scope.launch {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            _powerStatus.emit(
                PowerStatus.Available(
                    powerManager.isDeviceIdleMode,
                    powerManager.isPowerSaveMode,
                )
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PowerMonitor? = null

        fun getInstance(context: Context): PowerMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PowerMonitor(context).also { INSTANCE = it }
            }
        }
    }
}
