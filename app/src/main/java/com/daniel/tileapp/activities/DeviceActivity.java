package com.daniel.tileapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.daniel.tileapp.R;
import com.daniel.tileapp.tile.BluetoothTile;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceActivity extends AppCompatActivity implements BluetoothTile.BluetoothTileListener {
    @BindView(R.id.deviceNameTextView) TextView deviceNameTextView;

    private BluetoothTile connectedTile;
    private String deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        ButterKnife.bind(this);

        deviceName = getIntent().getExtras().getString(MainActivity.DEVICE_NAME);
        deviceNameTextView.setText(deviceName);
        String mac = getIntent().getExtras().getString(MainActivity.DEVICE_MAC);

        // Connect to the tile
        connectedTile = new BluetoothTile(mac, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectedTile.disconnect();
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

    @Override
    public void connected() {
        runOnUiThread(() -> deviceNameTextView.setText(deviceName + " - Connected"));
    }
}
