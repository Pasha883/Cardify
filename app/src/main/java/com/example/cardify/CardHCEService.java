package com.example.cardify;

import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CardHCEService extends HostApduService {

    private static final String TAG = "CardHceService";

    private static final byte[] SELECT_APDU_HEADER = {
            (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00
    };

    private static final byte[] STATUS_SUCCESS = {(byte) 0x90, (byte) 0x00};
    private static final byte[] STATUS_FAILED = {(byte) 0x6F, (byte) 0x00};

    public static String cardId = "cardID123";  // Должен быть static, чтобы быть доступным при любом запуске сервиса

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "HCE Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "HCE Service started");
        if (intent != null && intent.hasExtra("cardId")) {
            cardId = intent.getStringExtra("cardId");
            Log.d(TAG, "Card ID set: " + cardId);
        } else {
            Log.e(TAG, "No cardId provided in intent");
        }
        return START_STICKY;
    }

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        Log.d(TAG, "APDU received: " + Arrays.toString(apdu));
        if (Arrays.equals(Arrays.copyOfRange(apdu, 0, 4), SELECT_APDU_HEADER)) {
            byte[] response = concat(cardId.getBytes(StandardCharsets.UTF_8), STATUS_SUCCESS);
            Log.d(TAG, "Sending card ID response: " + cardId);
            return response;
        } else {
            Log.e(TAG, "Invalid APDU received, sending failure");
            return STATUS_FAILED;
        }
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "HCE Service deactivated, reason: " + reason);
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
