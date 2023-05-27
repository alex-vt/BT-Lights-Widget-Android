package com.alexvt.btlightswidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.benasher44.uuid.uuidFrom
import com.juul.kable.peripheral
import com.juul.kable.toIdentifier
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

class WidgetClickReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_COLOR = "EXTRA_COLOR"
    }

    private var colorSetJob: Job? = null

    override fun onReceive(context: Context, intent: Intent) {
        val color = intent.getIntExtra(EXTRA_COLOR, 0)
        val bluetoothMacAddresses = context.resources
            .getStringArray(R.array.bluetooth_mac_addresses).toList()

        colorSetJob?.cancel()
        colorSetJob = GlobalScope.launch(
            CoroutineExceptionHandler { _, exception ->
                Log.e("CoroutineExceptionHandler", "colorSetJob exception", exception)
            }
        ) {
            if (checkOrGetBluetoothPermission(context)) {
                setColor(
                    bluetoothMacAddresses,
                    red = (color shr 16 and 0xFF).toByte(),
                    green = (color shr 8 and 0xFF).toByte(),
                    blue = (color shr 0 and 0xFF).toByte(),
                )
            }
        }
    }

}

private suspend fun setColor(
    bluetoothMacAddresses: List<String>,
    red: Byte,
    green: Byte,
    blue: Byte
) {
    val characteristicUuidText = "0000ffd9-0000-1000-8000-00805f9b34fb"
    val data = byteArrayOf(
        0x56.toByte(),
        red,
        green,
        blue,
        0x00.toByte(), 0xF0.toByte(), 0xAA.toByte()
    )
    val bluetoothOperationTimeout = 2000L

    coroutineScope {
        bluetoothMacAddresses.map { bluetoothMacAddress ->
            peripheral(bluetoothMacAddress.toIdentifier()).run {
                withTimeout(bluetoothOperationTimeout) {
                    connect()
                }
                withTimeoutOrNull(bluetoothOperationTimeout) { // if timeout, still disconnect
                    services
                        ?.flatMap { it.characteristics }
                        ?.firstOrNull { it.characteristicUuid == uuidFrom(characteristicUuidText) }
                        ?.also { write(it, data) }
                }
                withTimeout(bluetoothOperationTimeout) {
                    disconnect()
                }
            }
        }
    }

}