package com.example.cardify;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

public class BluetoothDeviceHandler {
    private static final Set<String> connectedDevices = new HashSet<>();
    private static final Set<String> ignoredDevices = new HashSet<>();

    public static void loadData(Context context) {
        connectedDevices.addAll(SharedPrefsHelper.getSet(context, "connected_devices"));
        ignoredDevices.addAll(SharedPrefsHelper.getSet(context, "ignored_devices"));
    }

    public static boolean shouldIgnore(String deviceName) {
        return ignoredDevices.contains(deviceName);
    }

    public static boolean isAlreadyConnected(String deviceName) {
        return connectedDevices.contains(deviceName);
    }

    public static void addConnected(String deviceName, Context context) {
        connectedDevices.add(deviceName);
        SharedPrefsHelper.saveSet(context, "connected_devices", connectedDevices);
    }

    public static void addIgnored(String deviceName, Context context) {
        ignoredDevices.add(deviceName);
        SharedPrefsHelper.saveSet(context, "ignored_devices", ignoredDevices);
    }

    public static void clearAll(Context context) {
        connectedDevices.clear();
        ignoredDevices.clear();
        SharedPrefsHelper.saveSet(context, "connected_devices", connectedDevices);
        SharedPrefsHelper.saveSet(context, "ignored_devices", ignoredDevices);
    }
}

