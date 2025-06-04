package com.example.cardify;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

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
            BluetoothGattCharacteristic targetCharacteristic;
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                String mac = device.getAddress();

                updateDeviceList();

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
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                    }
                    gatt.discoverServices(); // ← запускаем поиск сервисов

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

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = gatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"));
                    if (service != null) {
                        targetCharacteristic = service.getCharacteristic(UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"));
                        if (targetCharacteristic != null) {
                            Log.d("BluetoothMang", "Чтение характеристики...");
                            gatt.readCharacteristic(targetCharacteristic); // читаем JSON
                        }
                    }
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    String jsonData = characteristic.getStringValue(0);
                    Log.d("BluetoothMang", "Получен JSON: " + jsonData);

                    // Тут распарсим JSON
                    parseAndLogJson(jsonData);
                }
            }

            private boolean parseAndLogJson(String jsonData) {
                try {
                    JSONObject json = new JSONObject(jsonData);
                    String serial = json.getString("serial");
                    int totalMemory = json.getInt("totalMemory");
                    int systemMemory = json.getInt("systemMemory");
                    int usedMemory = json.getInt("usedMemory");
                    int savedCards = json.getInt("savedCards");
                    int battery = json.getInt("battery");

                    Log.d("BluetoothMang", "Serial: " + serial);
                    Log.d("BluetoothMang", "Memory: " + usedMemory + "/" + totalMemory + " (sys: " + systemMemory + ")");
                    Log.d("BluetoothMang", "Cards: " + savedCards);
                    Log.d("BluetoothMang", "Battery: " + battery + "%");

                    BluetoothDeviceModel deviceModel = new BluetoothDeviceModel(device.getName(), device.getAddress(), usedMemory, totalMemory, savedCards, true, battery);;
                    BluetoothDeviceHandler.addOrUpdateDevice(context, device.getAddress(), deviceModel);

                    updateDeviceList();


                    // Если хочешь — передай эти данные во фрагмент через интерфейс
                } catch (JSONException e) {
                    Log.e("BluetoothMang", "Ошибка разбора JSON", e);
                    Log.d("BluetoothMang", "Чтение характеристики...");
                    return false;
                }

                return true;
            }


        });
    }

    public static boolean hasBluetoothPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED;
    }

    private static void updateDeviceList() {
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
    }
}


