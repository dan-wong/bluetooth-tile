package com.daniel.tileapp.util;

import android.bluetooth.le.ScanCallback;
import android.os.AsyncTask;

public class BluetoothScannerAsyncTask extends AsyncTask<Void, Void, Void> {
    private ScanCallback callback;

    public BluetoothScannerAsyncTask(ScanCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        BluetoothScannerUtil.scan(callback);
        return null;
    }
}
