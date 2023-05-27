package com.alexvt.btlightswidget

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alexvt.btlightswidget.BluetoothPermissionHandlingActivity.Companion.permissionOutcomeOrNull
import kotlinx.coroutines.delay

suspend fun checkOrGetBluetoothPermission(applicationContext: Context): Boolean {
    // an already granted permission won't trigger re-requesting
    if (
        ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        return true
    }
    // at this point permission is not granted, an overlay activity requests it
    applicationContext.startActivity(
        Intent(applicationContext, BluetoothPermissionHandlingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
    while (true) {
        val permissionPollIntervalMillis = 100L
        delay(permissionPollIntervalMillis) // not granted or denied yet...
        permissionOutcomeOrNull?.let { return it }
    }
}

class BluetoothPermissionHandlingActivity : Activity() {

    companion object {
        var permissionOutcomeOrNull: Boolean? = null
        private const val REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionOutcomeOrNull = null

        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            permissionOutcomeOrNull = true
        } else {
            permissionOutcomeOrNull = false
            Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT)
                .show()
        }
        finish()
    }
}