package com.daniel.tileapp.bluetoothscanner;

import android.bluetooth.le.ScanCallback;
import android.os.AsyncTask;

import static com.daniel.tileapp.bluetoothscanner.BluetoothScannerKt.bluetoothScan;

public class BluetoothScannerAsyncTask extends AsyncTask<Void, Void, Void> {
    private ScanCallback callback;

    public BluetoothScannerAsyncTask(ScanCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        bluetoothScan(callback);
        return null;
    }
}
