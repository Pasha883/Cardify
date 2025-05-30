package com.example.cardify;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class DeviceScanCallback extends ScanCallback {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        BluetoothDevice device = result.getDevice();
        String deviceName = device.getName();

        if (deviceName != null && deviceName.startsWith("Cardify_Touch_")) {
            if (!BluetoothDeviceHandler.shouldIgnore(deviceName)) {
                if (!BluetoothDeviceHandler.isAlreadyConnected(deviceName)) {
                    showDialogToUser(device);
                } else {
                    BluetoothConnectionManager.connectToDevice(device);
                }
            }
        }
    }

    private void showDialogToUser(BluetoothDevice device) {
        FragmentManager fm = MainActivity.getInstance().getSupportFragmentManager();
        Fragment existingDialog = fm.findFragmentByTag("connect_dialog");
        if (existingDialog == null) {
            DeviceConnectDialogFragment dialog = DeviceConnectDialogFragment.newInstance(device);
            dialog.show(MainActivity.getInstance().getSupportFragmentManager(), "connect_dialog");
        }
    }
}

