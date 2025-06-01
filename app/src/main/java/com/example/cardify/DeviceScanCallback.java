package com.example.cardify;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class DeviceScanCallback extends ScanCallback {
    private final Context context;

    public DeviceScanCallback(Context context) {
        this.context = context.getApplicationContext(); // безопаснее
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        BluetoothDevice device = result.getDevice();
        String deviceName = device.getName();
        String macAddress = device.getAddress();

        if (deviceName != null && deviceName.startsWith("Cardify_Touch_")) {
            if (!BluetoothDeviceHandler.shouldIgnore(macAddress)) {
                if (!BluetoothDeviceHandler.isAlreadyConnected(macAddress)) {
                    if (!BluetoothDeviceHandler.isDetected(macAddress)) {
                        BluetoothDeviceHandler.addDetected(device.getAddress());
                        showDialogToUser(device);
                    }
                } else {
                    if (!BluetoothDeviceHandler.isCurrentlyConnected(macAddress)){
                        BluetoothConnectionManager.connectToDevice(context, device);
                    }
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

