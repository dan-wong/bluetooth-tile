package com.daniel.tileapp.activities;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.daniel.tileapp.R;
import com.daniel.tileapp.misc.DividerItemDecoration;
import com.daniel.tileapp.tile.BluetoothTileRecyclerViewAdapter;
import com.daniel.tileapp.util.BluetoothScannerUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements BluetoothTileRecyclerViewAdapter.BluetoothTileListener {
    public static final String DEVICE_NAME = "com.daniel.tileapp.device.name";
    public static final String DEVICE_MAC = "com.daniel.tileapp.device.mac";

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.devicesRecyclerView) RecyclerView devicesRecyclerView;

    private MutableLiveData<List<BluetoothDevice>> devices = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        devicesRecyclerView.setLayoutManager(layoutManager);
        devicesRecyclerView.addItemDecoration(new DividerItemDecoration(getDrawable(R.drawable.drawable_divider)));

        final Observer<List<BluetoothDevice>> devicesObserver = bluetoothDevices -> devicesRecyclerView.setAdapter(new BluetoothTileRecyclerViewAdapter(bluetoothDevices, this));
        devices.observe(this, devicesObserver);
    }

    @OnClick(R.id.scanBtn)
    public void scanBtn() {
        BluetoothScannerUtil.scan(scanCallback);
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
        }
    };
}
