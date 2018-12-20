package com.daniel.tileapp.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.daniel.tileapp.R;
import com.daniel.tileapp.tile.BluetoothTile;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceActivity extends AppCompatActivity implements BluetoothTile.BluetoothTileListener {
    @BindView(R.id.deviceNameTextView) TextView deviceNameTextView;

    private String deviceName, mac;
    private BluetoothTile connectedTile;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        ButterKnife.bind(this);

        deviceName = Objects.requireNonNull(getIntent().getExtras()).getString(MainActivity.DEVICE_NAME);
        deviceNameTextView.setText(deviceName);
        mac = getIntent().getExtras().getString(MainActivity.DEVICE_MAC);

        attemptConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectedTile.disconnect();
        if (progressDialog != null) progressDialog.cancel();
    }

    private void attemptConnection() {
        connectedTile = new BluetoothTile(mac, this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.connecting_in_progress));
        progressDialog.show();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    runOnUiThread(() -> connectionFailed());
                }
            }
        }, 10000);
    }

    private void connectionFailed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.try_again)
                .setTitle(R.string.connection_failed);
        builder.setPositiveButton(R.string.yes, (dialog, id) -> attemptConnection());
        builder.setNegativeButton(R.string.no, (dialog, id) -> finish());
        builder.create().show();
    }

    @OnClick(R.id.alarmOnBtn)
    public void alarmOnBtn() {
        connectedTile.turnOnAlarm();
    }

    @OnClick(R.id.alarmOffBtn)
    public void alarmOffBtn() {
        connectedTile.turnOffAlarm();
    }

    @Override
    public void connected() {
        runOnUiThread(() -> deviceNameTextView.setText(deviceName + " - Connected"));
        progressDialog.dismiss();
        progressDialog = null;
    }
}
