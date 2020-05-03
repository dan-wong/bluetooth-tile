package com.daniel.tileapp.bluetoothscanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log

private const val TAG: String = "BluetoothScanner"
private val serviceUuid = ParcelUuid.fromString("00001802-0000-1000-8000-00805f9b34fb")

fun bluetoothScan(callback: ScanCallback?) {
    val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    val scanFilter = ScanFilter.Builder()
            .setServiceUuid(serviceUuid)
            .build()
    val scanFilters: List<ScanFilter> = listOf(scanFilter)
    val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

    Log.d(TAG, "Starting scan")
    bluetoothLeScanner.startScan(scanFilters, scanSettings, callback)

    Handler().postDelayed({
        bluetoothLeScanner.stopScan(callback)
        Log.d(TAG, "Stopping scan")
    }, 5000)
}
