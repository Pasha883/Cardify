package com.example.cardify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class DeviceConnectDialogFragment extends DialogFragment {
    private BluetoothDevice device;

    public static DeviceConnectDialogFragment newInstance(BluetoothDevice device) {
        DeviceConnectDialogFragment fragment = new DeviceConnectDialogFragment();
        fragment.device = device;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Подключить Cardify Touch?")
                .setMessage("Устройство " + device.getName() + " найдено рядом.")
                .setPositiveButton("Подключить", (dialog, which) -> {
                    BluetoothDeviceHandler.addConnected(device.getName(), getContext());
                    BluetoothConnectionManager.connectToDevice(device);
                })
                .setNegativeButton("Игнорировать", (dialog, which) -> {
                    BluetoothDeviceHandler.addIgnored(device.getName(), getContext());
                })
                .create();
    }
}

