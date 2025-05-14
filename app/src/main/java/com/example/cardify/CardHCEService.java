package com.example.cardify;

import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

// Импорт необходимых классов
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// CardHCEService расширяет HostApduService для эмуляции смарт-карты
public class CardHCEService extends HostApduService {

    // Тег для логирования
    private static final String TAG = "CardHceService";

    // Заголовок APDU для выбора AID (идентификатор приложения)
    private static final byte[] SELECT_APDU_HEADER = {
            (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00
    };

    // Байты статуса, указывающие на успешное выполнение операции
    private static final byte[] STATUS_SUCCESS = {(byte) 0x90, (byte) 0x00};
    // Байты статуса, указывающие на неудачное выполнение операции
    private static final byte[] STATUS_FAILED = {(byte) 0x6F, (byte) 0x00};

    // Статическая переменная для хранения идентификатора карты.
    // Она статическая, чтобы быть доступной при любом запуске сервиса.
    public static String cardId = "cardID123";  // Должен быть static, чтобы быть доступным при любом запуске сервиса

    @Override
    // Вызывается при создании сервиса
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "HCE Service created");
    }

    @Override
    // Вызывается при запуске сервиса
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "HCE Service started");
        // Проверяем, содержит ли интент дополнительный параметр "cardId"
        if (intent != null && intent.hasExtra("cardId")) {
            // Если да, обновляем идентификатор карты
            cardId = intent.getStringExtra("cardId");
            Log.d(TAG, "Card ID set: " + cardId);
        } else {
            Log.e(TAG, "No cardId provided in intent");
        }
        // Сервис будет перезапущен, если система убьет его
        return START_STICKY;
    }

    @Override
    // Вызывается при получении APDU-команды от считывателя NFC
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        Log.d(TAG, "APDU received: " + Arrays.toString(apdu));
        // Проверяем, начинается ли полученный APDU с заголовка SELECT_APDU_HEADER
        if (Arrays.equals(Arrays.copyOfRange(apdu, 0, 4), SELECT_APDU_HEADER)) {
            // Если это команда выбора, отвечаем идентификатором карты, за которым следует статус успеха
            byte[] response = concat(cardId.getBytes(StandardCharsets.UTF_8), STATUS_SUCCESS);
            Log.d(TAG, "Sending card ID response: " + cardId);
            return response;
        } else {
            Log.e(TAG, "Invalid APDU received, sending failure");
            return STATUS_FAILED;
        }
    }

    @Override
    // Вызывается при деактивации NFC-соединения
    public void onDeactivated(int reason) {
        Log.d(TAG, "HCE Service deactivated, reason: " + reason);
    }

    // Вспомогательный метод для конкатенации двух массивов байтов
    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
