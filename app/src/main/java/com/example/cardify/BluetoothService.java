package com.example.cardify;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.os.Looper;

import android.os.Handler;

public class BluetoothService {
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
    private final DeviceScanCallback scanCallback = new DeviceScanCallback();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long SCAN_PERIOD = 3000; // 10 секунд

    private boolean isScanning = false;

    public void startScanning() {
        isScanning = true;
        scanLoop();
    }

    private void scanLoop() {
        if (!isScanning) return;

        scanner.startScan(scanCallback);

        handler.postDelayed(() -> {
            scanner.stopScan(scanCallback);
            handler.postDelayed(this::scanLoop, 3000); // Подождать 2 сек перед следующим сканированием
        }, SCAN_PERIOD);
    }

    public void stopScanning() {
        isScanning = false;
        scanner.stopScan(scanCallback);
        handler.removeCallbacksAndMessages(null);
    }

    public void manualScan() {
        if (scanner != null) {
            scanner.startScan(scanCallback);
        }
    }
}

