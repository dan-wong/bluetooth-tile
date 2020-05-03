package com.daniel.tileapp.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daniel.tileapp.R;
import com.daniel.tileapp.bluetoothscanner.BluetoothScannerAsyncTask;
import com.daniel.tileapp.misc.DividerItemDecoration;
import com.daniel.tileapp.tile.BluetoothTileRecyclerViewAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainActivity extends AppCompatActivity implements BluetoothTileRecyclerViewAdapter.BluetoothTileListener {
    public static final String DEVICE_NAME = "com.daniel.tileapp.device.name";
    public static final String DEVICE_MAC = "com.daniel.tileapp.device.mac";

    private final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.devicesRecyclerView) RecyclerView devicesRecyclerView;
    @BindView(R.id.fab) FloatingActionButton fab;

    private MutableLiveData<List<BluetoothDevice>> devices = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        fab.setOnClickListener(view -> checkBluetoothEnabled());

        /* Setup Bluetooth device recycler view */
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        devicesRecyclerView.setLayoutManager(layoutManager);
        devicesRecyclerView.addItemDecoration(new DividerItemDecoration(getDrawable(R.drawable.drawable_divider)));

        final Observer<List<BluetoothDevice>> devicesObserver = bluetoothDevices -> devicesRecyclerView.setAdapter(new BluetoothTileRecyclerViewAdapter(bluetoothDevices, this));
        devices.observe(this, devicesObserver);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    private void startBluetoothScan() {
        if (EasyPermissions.hasPermissions(this, ACCESS_COARSE_LOCATION)) {
            new BluetoothScannerAsyncTask(scanCallback).execute();
            Toast.makeText(this, "Scanning...", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the coarse location permission", REQUEST_LOCATION_PERMISSION, ACCESS_COARSE_LOCATION);
        }
    }

    private void checkBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }  else {
            startBluetoothScan();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    startBluetoothScan();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "Bluetooth needs to be enabled!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void tileSelected(BluetoothDevice device) {
        Intent intent = new Intent(this, DeviceActivity.class);
        intent.putExtra(DEVICE_NAME, device.getName());
        intent.putExtra(DEVICE_MAC, device.getAddress());
        startActivity(intent);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (devices.getValue() == null) devices.setValue(new ArrayList<>());
            if (!devices.getValue().contains(result.getDevice())) {
                List<BluetoothDevice> newDeviceList = devices.getValue();
                newDeviceList.add(result.getDevice());

                devices.postValue(newDeviceList);
            }

            Toast.makeText(getApplicationContext(), "Scan complete", Toast.LENGTH_SHORT).show();
        }
    };
}
