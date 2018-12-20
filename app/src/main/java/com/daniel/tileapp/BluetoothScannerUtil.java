package com.daniel.tileapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BluetoothScannerUtil {
    private static final String TAG = BluetoothScannerUtil.class.getSimpleName();
    private static final ParcelUuid serviceUuid = ParcelUuid.fromString("00001802-0000-1000-8000-00805f9b34fb");

    public static void scan(ScanCallback callback) {
        BluetoothLeScanner bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();

        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
        scanFilterBuilder.setServiceUuid(serviceUuid);

        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(scanFilterBuilder.build());

        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);

        Log.d(TAG, "Starting scan");
        bluetoothLeScanner.startScan(scanFilters, scanSettingsBuilder.build(), callback);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(callback);
                Log.d(TAG, "Stopping scan");
            }
        }, 5000);
    }
}
