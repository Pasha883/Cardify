package com.example.cardify;

import static com.example.cardify.AppContextProvider.getContext;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;

import com.example.cardify.AppContextProvider;

public class BluetoothConnectionManager {
    public static void connectToDevice(BluetoothDevice device) {
        device.connectGatt(getContext(), false, new BluetoothGattCallback() {
            // Реализация GATT-колбэков
        });
    }
}

