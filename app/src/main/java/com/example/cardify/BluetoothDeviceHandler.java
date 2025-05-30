package com.example.cardify;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

public class BluetoothDeviceHandler {
    private static final String PREFS_NAME = "bluetooth_device_storage";
    private static final String DEVICES_KEY = "devices";

    private static Map<String, BluetoothDeviceModel> deviceMap = new HashMap<>();
    private static final Gson gson = new Gson();

    private static final Set<String> connectedDevices = new HashSet<>();
    private static final Set<String> ignoredDevices = new HashSet<>();
    private static final Set<String> connectedDevicesInModel = new HashSet<>();

    public static void loadData(Context context) {
        connectedDevices.addAll(SharedPrefsHelper.getSet(context, "connected_devices"));
        ignoredDevices.addAll(SharedPrefsHelper.getSet(context, "ignored_devices"));
        connectedDevicesInModel.addAll(SharedPrefsHelper.getSet(context, "connected_devices_in_model"));

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(DEVICES_KEY, null);

        if (json != null) {
            Type type = new TypeToken<HashMap<String, BluetoothDeviceModel>>() {}.getType();
            deviceMap = gson.fromJson(json, type);
        }
    }

    public static Set<String> getConnectedDevices() {
        return connectedDevices;
    }

    public static boolean shouldIgnore(String deviceAddress) {
        return ignoredDevices.contains(deviceAddress);
    }

    public static boolean isAlreadyConnected(String macAddress) {
        return connectedDevices.contains(macAddress);
    }

    public static void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = gson.toJson(deviceMap);
        editor.putString(DEVICES_KEY, json);
        editor.apply();
    }

    public static void addOrUpdateDevice(Context context, String macAddress, BluetoothDeviceModel model) {
        deviceMap.put(macAddress, model);
        save(context);
    }

    public static void addConnected(String macAddress, Context context) {
        connectedDevices.add(macAddress);
        SharedPrefsHelper.saveSet(context, "connected_devices", connectedDevices);
    }

    public static BluetoothDeviceModel getDevice(String macAddress) {
        return deviceMap.get(macAddress);
    }


    public static void addIgnored(String macAddress, Context context) {
        ignoredDevices.add(macAddress);
        SharedPrefsHelper.saveSet(context, "ignored_devices", ignoredDevices);
    }

    public static void clearAll(Context context) {
        connectedDevices.clear();
        ignoredDevices.clear();
        SharedPrefsHelper.saveSet(context, "connected_devices", connectedDevices);
        SharedPrefsHelper.saveSet(context, "ignored_devices", ignoredDevices);

        deviceMap.clear();
        save(context);
    }

    public static boolean hasSavedDevices(Context context) {
        Set<String> savedDevices = context.getSharedPreferences("bluetooth_devices", Context.MODE_PRIVATE)
                .getStringSet("devices", new HashSet<>());
        return savedDevices != null && !savedDevices.isEmpty();
    }

    public static List<BluetoothDeviceModel> getAllDevices(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        List<BluetoothDeviceModel> deviceList = new ArrayList<>();
        Gson gson = new Gson();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String json = (String) entry.getValue();
            BluetoothDeviceModel device = gson.fromJson(json, BluetoothDeviceModel.class);
            deviceList.add(device);
        }

        return deviceList;
    }


}

