package com.daniel.tileapp.tile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.daniel.tileapp.BaseApplication;

import java.util.UUID;

public class BluetoothTile {
    private static final String TAG = BluetoothTile.class.getSimpleName();
    private static final UUID RX_ALARM_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    private static final UUID RX_CHAR_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");

    private final BluetoothDevice bluetoothDevice;
    private final BluetoothGatt bluetoothGatt;

    public BluetoothTile(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bluetoothDevice.getAddress());
        this.bluetoothGatt = bluetoothDevice.connectGatt(BaseApplication.getInstance(), false, gattCallback);
    }

    public void disconnect() {
        if (bluetoothGatt != null) bluetoothGatt.disconnect();
    }

    public void turnOnAlarm() {
        writeRXCharacteristic(new byte[]{(byte) 2}, bluetoothGatt);
    }

    public void turnOffAlarm() {
        writeRXCharacteristic(new byte[1], bluetoothGatt);
    }

    private void writeRXCharacteristic(byte[] data, final BluetoothGatt bluetoothGatt) {
        String mac = bluetoothGatt.getDevice().getAddress();
        BluetoothGattService rxService = bluetoothGatt.getService(RX_ALARM_UUID);
        final BluetoothGattCharacteristic rxChar = rxService.getCharacteristic(RX_CHAR_UUID);

        rxChar.setValue(data);
        Log.d(TAG, "Written txchar to " + mac + " status = " + bluetoothGatt.writeCharacteristic(rxChar));
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == 0) {
                Log.d(TAG, "Disconnected");
            } else if (newState == 2) {
                Log.d(TAG, "Connected - beginning service scan");
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered");
            } else {
                Log.d(TAG, "onServicesDiscovered received: " + status);
            }
        }
    };
}
