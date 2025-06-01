package com.example.cardify;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class BluetoothConnectionManager {

    public static void connectToDevice(Context context, BluetoothDevice device) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.e("BluetoothMang", "No permission");
                return;
            }
        }
        device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                String mac = device.getAddress();




                FragmentManager fragmentManager = MainActivity.getInstance().getSupportFragmentManager();
                MyDevicesFragment fragment = (MyDevicesFragment) fragmentManager.findFragmentByTag("MY_DEVICES_FRAGMENT");

                if (fragment != null) {
                    //fragment.updateDeviceList();
                    fragmentManager.beginTransaction()
                            .remove(fragment)
                            .commit();
                    fragmentManager.beginTransaction()
                            .add(R.id.fragment_container, new MyDevicesFragment(), "MY_DEVICES_FRAGMENT")
                            .commit();

                    Log.d("BluetoothMang", "MyDevicesFragment updated OK");
                }

                //Log.d("BluetoothMang", "MyDevicesFragment updated");


                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BluetoothMang", "Устройство подключено: " + mac);

                    /*BluetoothDeviceModel model = BluetoothDeviceHandler.getDevice(mac);
                    if (model != null) {
                        model.setConnected(true);
                        BluetoothDeviceHandler.addOrUpdateDevice(context, mac, model);
                    }*/

                    BluetoothDeviceHandler.addConnected(mac, context);
                    BluetoothDeviceHandler.addCurrentlyConnected(mac);
                    // Дополнительно можно запустить discoverServices
                    // gatt.discoverServices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BluetoothMang", "Устройство отключено: " + mac);

                    /*BluetoothDeviceModel model = BluetoothDeviceHandler.getDevice(mac);
                    if (model != null) {
                        model.setConnected(false);
                        BluetoothDeviceHandler.addOrUpdateDevice(context, mac, model);
                    }*/

                    BluetoothDeviceHandler.removeCurrentlyConnected(mac);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Log.e("BluetoothMang", "No permission");
                            return;
                        }
                    }

                    gatt.close(); // освободить ресурсы
                }
            }

            // По желанию: переопредели onServicesDiscovered, если читаешь характеристики
        });
    }

    public static boolean hasBluetoothPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED;
    }
}


