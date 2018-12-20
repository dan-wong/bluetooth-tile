package com.daniel.tileapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    public static final UUID RX_ALART_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_CHAR_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");
    private static final String TAG = MainActivity.class.getSimpleName();
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == 0) {
                Log.i(TAG, "Disconnected");
            } else if (newState == 2) {
                Log.i(TAG, "Connected - beginning service scan");
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services: " + gatt.getServices().size());
            } else {
                Log.i(TAG, "onServicesDiscovered received: " + status);
            }
        }
    };
    @BindView(R.id.devicesTextView)
    TextView devicesTextView;
    private List<BluetoothDevice> devices = new ArrayList<>();
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
    private BluetoothGatt bluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(devices.get(0).getAddress());
        device.connectGatt(this, true, gattCallback).disconnect();
    }

    @OnClick(R.id.scanBtn)
    public void scanBtn() {
        BluetoothScannerUtil.scan(scanCallback);
    }

    @OnClick(R.id.connectBtn)
    public void connectBtn() {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(devices.get(0).getAddress());
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
    }

    @OnClick(R.id.alarmOnBtn)
    public void alarmOnBtn() {
        turnOnAlarm(bluetoothGatt);
    }

    @OnClick(R.id.alarmOffBtn)
    public void alarmOffBtn() {
        turnOffAlarm(bluetoothGatt);
    }

    private void turnOnAlarm(BluetoothGatt bluetoothGatt) {
        writeRXCharacteristic(new byte[]{(byte) 2}, bluetoothGatt);
    }

    private void turnOffAlarm(BluetoothGatt bluetoothGatt) {
        writeRXCharacteristic(new byte[1], bluetoothGatt);
    }

    private void writeRXCharacteristic(byte[] data, final BluetoothGatt bluetoothGatt) {
        String mac = bluetoothGatt.getDevice().getAddress();
        BluetoothGattService rxService = bluetoothGatt.getService(RX_ALART_UUID);
        final BluetoothGattCharacteristic rxChar = rxService.getCharacteristic(RX_CHAR_UUID);

        rxChar.setValue(data);
        Log.i(TAG, "Written txchar to " + mac + " status = " + bluetoothGatt.writeCharacteristic(rxChar));
    }
}
