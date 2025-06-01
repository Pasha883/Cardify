#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include "qrcoderm.h"

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#define SERVICE_UUID        "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
#define CHARACTERISTIC_UUID "6e400003-b5a3-f393-e0a9-e50e24dcca9e"

BLECharacteristic *pCharacteristic;
BLEServer *pServer;
BLEAdvertising *pAdvertising;

bool deviceConnected = false;
bool dataSent = false;

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

#define QR_VERSION 3
#define QR_SCALE 2
#define GLOW_PADDING 3

#define BUTTON_LEFT 12
#define BUTTON_RIGHT 14
#define BUTTON_BACK 27
#define BUTTON_OK 26

class Button {
  public:
    Button (byte pin) {
      _pin = pin;
      pinMode(_pin, INPUT_PULLUP);
    }
    bool click() {
      bool btnState = digitalRead(_pin);
      if (!btnState && !_flag && millis() - _tmr >= 150) {
        _flag = true;
        _tmr = millis();
        return true;
      }
      /*
      if (!btnState && _flag && millis() - _tmr >= 500) {
        _tmr = millis ();
        return true;
      }
      */
      if (btnState && _flag) {
        _flag = false;
        _tmr = millis();
      }
      
      return false;
    }
  private:
    byte _pin;
    uint32_t _tmr;
    bool _flag;
};

class MyServerCallbacks : public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) override {
        deviceConnected = true;
        dataSent = false; // Сбрасываем флаг отправки
        Serial.println("[ESP32] Устройство подключено");
        pAdvertising->stop();
    }

    void onDisconnect(BLEServer* pServer) override {
        deviceConnected = false;
        dataSent = false;
        Serial.println("[ESP32] Устройство отключено");
        delay(500);
        pAdvertising->start();
        Serial.println("[ESP32] Реклама возобновлена");
    }
};

void sendJsonData() {
    //if (!deviceConnected || dataSent) return;

    // Формируем JSON данные
    String json = "{\"device\":\"Cardify_Touch_00001\","
                  "\"cardId\":\"cardID003\","
                  "\"buttons\":[\"left\",\"right\",\"back\",\"ok\"]}";

    // Устанавливаем значение характеристики
    pCharacteristic->setValue(json.c_str());
    
    // Отправляем через notify
    pCharacteristic->notify();
    dataSent = true;
    Serial.println("[ESP32] JSON данные отправлены");
}

Button btnLeft(BUTTON_LEFT);
Button btnRight(BUTTON_RIGHT);
Button btnBack(BUTTON_BACK);
Button btnOk(BUTTON_OK);

void setup() {
  Serial.begin(115200);

  //pinMode(BUTTON_LEFT, INPUT_PULLUP);
  //pinMode(BUTTON_RIGHT, INPUT_PULLUP);
  //pinMode(BUTTON_BACK, INPUT_PULLUP);
  //pinMode(BUTTON_OK, INPUT_PULLUP);

  Serial.println("[ESP32] Запуск BLE и рекламы");
  BLEDevice::init("Cardify_Touch_00001");

  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  BLEService *pService = pServer->createService(SERVICE_UUID);

  pCharacteristic = pService->createCharacteristic(
      CHARACTERISTIC_UUID,
      BLECharacteristic::PROPERTY_READ |
      BLECharacteristic::PROPERTY_NOTIFY |
      BLECharacteristic::PROPERTY_WRITE
  );

  pCharacteristic->addDescriptor(new BLE2902());
  pCharacteristic->setValue("Cardify Ready");
  pService->start();

  pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->start();

  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println(F("SSD1309 not found"));
    while (true);
  }

  display.setRotation(1); // Повернуть на 90° по часовой
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);

  String url = "https://cardify.page.link/add?cardId=cardID003";
  generateAndDrawQR(url, " Cardify Inc.");
}

void loop() {

  if (deviceConnected && !dataSent) {
      // Даем клиенту немного времени на подписку, прежде чем отправить данные.
      // Это "костыль", лучше, если клиент явно запрашивает данные или ESP32 ждет
      // подтверждения подписки, но для простой отправки при подключении это может помочь.
      delay(1000); // Задержка 500 мс. Можете подобрать значение.
      sendJsonData();
  }

  if(btnOk.click()){
    Serial.println("data button");
    sendJsonData();
  }

  if(btnBack.click()){
    Serial.println("Button Back");
  }

  if(btnLeft.click()){
    Serial.println("Button Left");
  }

  if(btnRight.click()){
    Serial.println("Button Right");
  }
}

void generateAndDrawQR(String content, String title) {
  QRCode qrcode;
  uint8_t qrcodeData[qrcode_getBufferSize(QR_VERSION)];
  qrcode_initText(&qrcode, qrcodeData, QR_VERSION, ECC_LOW, content.c_str());

  int qrSize = qrcode.size * QR_SCALE;


  

  display.clearDisplay();

  // Название компании сверху
  int letters_in_line = 9;
  int text_height = 8;

  int lines = title.length() % letters_in_line > 0 ? title.length() / letters_in_line + 1 : title.length() / letters_in_line; 

  int pointer = 0;

  for (int i = 0; i < lines; i++){
    String cur = "";

    while (pointer < title.length() && cur.length() < letters_in_line){
      cur += title[pointer];
      pointer++;
    }
    display.setCursor((SCREEN_HEIGHT - cur.length() * 6) / 2, text_height * i);
    display.println(cur);
  }

  int xOffset = 0;
  int yOffset = text_height * (lines + 1) > 32 ? text_height * (lines + 1) : 32;  // Оставим место под текст

  //display.setCursor((SCREEN_HEIGHT - title.length()) / 2, 0);
  //display.println(title);

  // Подложка
  display.fillRect(0, yOffset - GLOW_PADDING,
                   qrSize + 2 * GLOW_PADDING, qrSize + 2 * GLOW_PADDING,
                   SSD1306_WHITE);

  // QR-код
  for (uint8_t y = 0; y < qrcode.size; y++) {
    for (uint8_t x = 0; x < qrcode.size; x++) {
      if (qrcode_getModule(&qrcode, x, y)) {
        display.fillRect(xOffset + GLOW_PADDING + x * QR_SCALE,
                         yOffset + y * QR_SCALE,
                         QR_SCALE, QR_SCALE, SSD1306_BLACK);
      }
    }
  }

  /*
  if (yOffset + GLOW_PADDING + qrSize < 112){
    String text = "Scan";
    display.setTextSize(2);
    display.setCursor((SCREEN_HEIGHT - text.length() * 11) / 2, 112);
    display.println(text);
  }
  */

  display.display();
}
