package com.daniel.tileapp.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private static final String TAG = DeviceActivity.class.getSimpleName();

    @BindView(R.id.deviceNameTextView) TextView deviceNameTextView;
    @BindView(R.id.macTextView) TextView macTextView;
    @BindView(R.id.connectionTextView) TextView connectionTextView;
    @BindView(R.id.rssiTextView) TextView rssiTextView;
    @BindView(R.id.batteryTextView) TextView batteryTextView;
    @BindView(R.id.toggleAlarmBtn) ImageButton toggleAlarmBtn;
    @BindView(R.id.editDeviceNameBtn) ImageButton editDeviceNameBtn;
    @BindView(R.id.disconnectBtn) Button disconnectBtn;

    private String deviceName, mac;
    private boolean alarmOn = false, connected = true;
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
        macTextView.setText(mac);

        toggleAlarmBtn.setOnClickListener(toggleAlarmBtnOnClickListener);
        disconnectBtn.setOnClickListener(toggleDisconnectBtnOnClickListener);

        attemptConnection();

        batteryTextView.setOnClickListener(view -> connectedTile.readBatteryLevel());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectedTile != null) connectedTile.disconnect();
        if (progressDialog != null) progressDialog.cancel();
    }

    private void disconnectedPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.disconnected);
        builder.setMessage("You are not currently connected!")
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void turnOnAlarm() {
        connectedTile.turnOnAlarm();
        toggleAlarmBtn.setImageResource(R.drawable.ic_notifications_off_white_24dp);
        alarmOn = true;

        connectedTile.readRemoteRssi();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> turnOffAlarm());
            }
        }, 10000);
    }

    private void turnOffAlarm() {
        connectedTile.turnOffAlarm();
        toggleAlarmBtn.setImageResource(R.drawable.ic_notifications_active_white_24dp);
        alarmOn = false;
        connectedTile.readBatteryLevel();
    }

    private void attemptConnection() {
        connectedTile = new BluetoothTile(mac, this);

        showConnectionProgressDialog();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    runOnUiThread(DeviceActivity.this::connectionFailed);
                }
            }
        }, 8000);
    }

    private void showConnectionProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.connecting_in_progress));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void connectionFailed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.connection_failed);
        builder.setMessage(R.string.try_again)
                .setPositiveButton(R.string.yes, (dialog, id) -> attemptConnection())
                .setNegativeButton(R.string.no, (dialog, id) -> finish());
        builder.create().show();
    }

    @OnClick(R.id.editDeviceNameBtn)
    public void editDeviceNameBtn() {
        if (!connected) {
            disconnectedPrompt();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.input_device_name_dialog, null);
        builder.setView(view)
                .setPositiveButton("Save", (dialog, id) -> {
                    EditText deviceNameEditText = view.findViewById(R.id.deviceNameEditText);
                    deviceName = deviceNameEditText.getText().toString();

                    if (TextUtils.isEmpty(deviceName)) {
                        deviceNameEditText.setError("Device Name must not be empty!");
                    } else {
                        deviceNameTextView.setText(deviceName);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());
        builder.setTitle("Change Device Name");
        builder.create().show();
    }

    @Override
    public void connected() {
        runOnUiThread(() -> {
            connectionTextView.setText(getString(R.string.connected));
            disconnectBtn.setText(getString(R.string.disconnect));
        });
        progressDialog.dismiss();
        progressDialog = null;
        connected = true;

        connectedTile.readBatteryLevel();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                connectedTile.readRemoteRssi();
            }
        }, 2000);
    }

    @Override
    public void disconnected() {
        runOnUiThread(() -> {
            connectionTextView.setText(R.string.disconnected);
            disconnectBtn.setText(R.string.connect);
        });
        connected = false;
    }

    @Override
    public void remoteRssi(int rssi) {
        runOnUiThread(() -> rssiTextView.setText(String.valueOf(rssi)));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (connected) {
                    connectedTile.readRemoteRssi();
                }
            }
        }, 5000);
    }

    @Override
    public void batteryLevel(int level) {
        runOnUiThread(() -> batteryTextView.setText(String.format("%d%%", level)));
    }

    private final View.OnClickListener toggleAlarmBtnOnClickListener = view -> {
        if (!connected) {
            disconnectedPrompt();
            return;
        }

        if (alarmOn) {
            turnOffAlarm();
        } else {
            turnOnAlarm();
        }
    };

    private final View.OnClickListener toggleDisconnectBtnOnClickListener = view -> {
        if (connected) {
            connectedTile.disconnect();
            connected = false;
        } else {
            attemptConnection();
        }
    };
}
