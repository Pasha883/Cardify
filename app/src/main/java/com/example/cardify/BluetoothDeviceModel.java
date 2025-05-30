package com.example.cardify;

public class BluetoothDeviceModel {
    private String deviceName;
    private String macAddress; // ← Новый параметр
    private float usedMemoryMb;
    private float totalMemoryMb;
    private int savedCardsCount;
    private boolean isConnected;
    private int batteryLevel;

    public BluetoothDeviceModel(String deviceName, String macAddress, float usedMemoryMb, float totalMemoryMb,
                                int savedCardsCount, boolean isConnected, int batteryLevel) {
        this.deviceName = deviceName;
        this.macAddress = macAddress;
        this.usedMemoryMb = usedMemoryMb;
        this.totalMemoryMb = totalMemoryMb;
        this.savedCardsCount = savedCardsCount;
        this.isConnected = isConnected;
        this.batteryLevel = batteryLevel;
    }

    public String getDeviceName() { return deviceName; }
    public String getMacAddress() { return macAddress; }
    public float getUsedMemoryMb() { return usedMemoryMb; }
    public float getTotalMemoryMb() { return totalMemoryMb; }
    public int getSavedCardsCount() { return savedCardsCount; }
    public boolean isConnected() { return isConnected; }
    public int getBatteryLevel() { return batteryLevel; }

    public void setConnected(boolean connected) { isConnected = connected; }
    public void setBatteryLevel(int batteryLevel) { this.batteryLevel = batteryLevel; }
}


