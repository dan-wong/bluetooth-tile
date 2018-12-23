package com.daniel.tileapp.tile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.daniel.tileapp.BaseApplication;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class BluetoothTile {
    private static final String TAG = BluetoothTile.class.getSimpleName();

    /* Constants for Device alarm */
    private static final UUID RX_ALARM_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    private static final UUID RX_CHAR_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");

    /* Constants for Receiving key press */
    private static final UUID RX_KEY_PRESS_UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
    private static final UUID RX_KEY_UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");

    /* Constants for getting the battery level of the device */
    private static final UUID BATTERY_LEVEL_UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");
    private static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");

    /* Constant for setting notification on device */
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final BluetoothGatt bluetoothGatt;
    private List<BluetoothTileListener> listeners = new LinkedList<>();

    public BluetoothTile(String mac) {
        BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == 0) {
                    Log.d(TAG, "Disconnected");
                    listeners.forEach(BluetoothTileListener::disconnected);
                } else if (newState == 2) {
                    Log.d(TAG, "Connected - beginning service scan "  + gatt.discoverServices());
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Services discovered");

                    setKeyPressNotification();
                    listeners.forEach(BluetoothTileListener::connected);
                } else {
                    Log.d(TAG, "onServicesDiscovered received: " + status);
                }
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                listeners.forEach(l -> l.remoteRssi(rssi));
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.d(TAG, Arrays.toString(characteristic.getValue()) + " - Status: " + status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                Log.i(TAG, Arrays.toString(characteristic.getValue()));
            }
        };

        this.bluetoothGatt = bluetoothDevice.connectGatt(BaseApplication.getInstance(), false, gattCallback);
    }

    public BluetoothTile(String mac, BluetoothTileListener listener) {
        this(mac);
        listeners.add(listener);
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

    public void setKeyPressNotification() {
        BluetoothGattService keyService = bluetoothGatt.getService(RX_KEY_UUID);
        BluetoothGattCharacteristic keyPressCharacteristic = keyService.getCharacteristic(RX_KEY_PRESS_UUID);

        BluetoothGattDescriptor descriptor = keyPressCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);

        this.bluetoothGatt.setCharacteristicNotification(keyPressCharacteristic, true);
    }

    public void readBatteryLevel() {
        BluetoothGattService batteryService = bluetoothGatt.getService(BATTERY_SERVICE_UUID);
        BluetoothGattCharacteristic batteryLevelCharacteristic = batteryService.getCharacteristic(BATTERY_LEVEL_UUID);

        this.bluetoothGatt.readCharacteristic(batteryLevelCharacteristic);
    }

    public void readRemoteRssi() {
        bluetoothGatt.readRemoteRssi();
    }

    private void writeRXCharacteristic(byte[] data, final BluetoothGatt bluetoothGatt) {
        String mac = bluetoothGatt.getDevice().getAddress();
        BluetoothGattService rxService = bluetoothGatt.getService(RX_ALARM_UUID);
        if (rxService == null) return;

        BluetoothGattCharacteristic rxChar = rxService.getCharacteristic(RX_CHAR_UUID);
        if (rxChar == null) return;

        rxChar.setValue(data);
        Log.d(TAG, "Written txchar to " + mac + " status = " + bluetoothGatt.writeCharacteristic(rxChar));
    }

    public interface BluetoothTileListener {
        void connected();
        void disconnected();
        void remoteRssi(int rssi);
    }
}
