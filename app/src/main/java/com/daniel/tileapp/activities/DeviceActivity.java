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
    @BindView(R.id.toggleAlarmBtn) ImageButton toggleAlarmBtn;
    @BindView(R.id.disconnectBtn) Button disconnectBtn;

    private String deviceName, mac;
    private boolean alarmOn = false;
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

        attemptConnection();

        toggleAlarmBtn.setOnClickListener(view -> {
            if (alarmOn) {
                turnOffAlarm();
            } else {
                turnOnAlarm();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectedTile.disconnect();
        if (progressDialog != null) progressDialog.cancel();
    }

    private void turnOnAlarm() {
        connectedTile.turnOnAlarm();
        toggleAlarmBtn.setImageResource(R.drawable.ic_notifications_off_white_24dp);
        alarmOn = true;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    turnOffAlarm();
                });
            }
        }, 10000);
    }

    private void turnOffAlarm() {
        connectedTile.turnOffAlarm();
        toggleAlarmBtn.setImageResource(R.drawable.ic_notifications_active_white_24dp);
        alarmOn = false;
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
        runOnUiThread(() -> connectionTextView.setText(getString(R.string.connected)));
        progressDialog.dismiss();
        progressDialog = null;
    }
}
