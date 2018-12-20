package com.daniel.tileapp.activities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.daniel.tileapp.R;
import com.daniel.tileapp.tile.BluetoothTile;
import com.daniel.tileapp.util.BluetoothScannerUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.devicesTextView) TextView devicesTextView;

    private List<BluetoothDevice> devices = new ArrayList<>();
    private BluetoothTile connectedTile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectedTile != null) connectedTile.disconnect();
    }

    @OnClick(R.id.scanBtn)
    public void scanBtn() {
        BluetoothScannerUtil.scan(scanCallback);
    }

    @OnClick(R.id.connectBtn)
    public void connectBtn() {
        if (devices.size() == 0) {
            Toast.makeText(this, "No devices found!", Toast.LENGTH_SHORT).show();
            return;
        }
        connectedTile = new BluetoothTile(devices.get(0));
    }

    @OnClick(R.id.alarmOnBtn)
    public void alarmOnBtn() {
        if (connectedTile == null) {
            Toast.makeText(this, "Device not connected!", Toast.LENGTH_SHORT).show();
            return;
        }

        connectedTile.turnOnAlarm();
    }

    @OnClick(R.id.alarmOffBtn)
    public void alarmOffBtn() {
        if (connectedTile == null) {
            Toast.makeText(this, "Device not connected!", Toast.LENGTH_SHORT).show();
            return;
        }

        connectedTile.turnOffAlarm();
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (!devices.contains(result.getDevice())) {
                devices.add(result.getDevice());

                StringBuilder sb = new StringBuilder();
                sb.append("Device Name: ").append(result.getDevice().getName()).append("\n");
                sb.append("Device Address: ").append(result.getDevice().getAddress()).append("\n");
                sb.append("Device RSSI: ").append(result.getRssi()).append("\n");

                devicesTextView.setText(sb.toString());
            }
        }
    };
}
