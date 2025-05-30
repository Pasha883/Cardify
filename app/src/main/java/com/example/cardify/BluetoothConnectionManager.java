package com.example.cardify;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class BluetoothConnectionManager {

    public static void connectToDevice(Context context, BluetoothDevice device) {
        device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                String mac = device.getAddress();

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BluetoothMang", "Устройство подключено: " + mac);

                    BluetoothDeviceModel model = BluetoothDeviceHandler.getDevice(mac);
                    if (model != null) {
                        model.setConnected(true);
                        BluetoothDeviceHandler.addOrUpdateDevice(context, mac, model);
                    }

                    BluetoothDeviceHandler.addConnected(mac, context);
                    // Дополнительно можно запустить discoverServices
                    // gatt.discoverServices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BluetoothMang", "Устройство отключено: " + mac);

                    BluetoothDeviceModel model = BluetoothDeviceHandler.getDevice(mac);
                    if (model != null) {
                        model.setConnected(false);
                        BluetoothDeviceHandler.addOrUpdateDevice(context, mac, model);
                    }
                    gatt.close(); // освободить ресурсы
                }
            }

            // По желанию: переопредели onServicesDiscovered, если читаешь характеристики
        });
    }
}


