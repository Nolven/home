#include <Wire.h>
#include <Arduino.h>
#include <DHT.h>
#include <Adafruit_SSD1306.h>

// DHT (humidity/temperature)
#define DHTPIN 7
#define DHTTYPE DHT11   // DHT 11

// t6703 (CO2)
constexpr byte address      = 0x15;
constexpr byte requestSize  = 0x05;
constexpr byte responseSize = 0x04;
constexpr byte responseDelay = 0x0A;
constexpr byte fieldFunctionCode =  0x00;
constexpr byte fieldBytesNumber  =  0x01;
constexpr byte fieldDataMsb = 0x02;
constexpr byte fieldDataLsb = 0x03;

// SSD1306 screen
#define SCREEN_WIDTH 128 // OLED display width, in pixels
#define SCREEN_HEIGHT 64 // OLED display height, in pixels
byte screenStatus = SSD1306_DISPLAYON;
bool statusChanged = false;

void (* reset)() = nullptr;

// Common
#define DELAY 2000 // min delay for sensors between data send
#define CURR_ADDRESS 0x22 // this board

//////////////////////////////////////

// buffers
float h;
float t;
float hic;
float co2;

DHT dht(DHTPIN, DHTTYPE);

// Declaration for an SSD1306 display connected to I2C (SDA, SCL pins)
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);

// t6703-5k
uint16_t getT67XXMetric(uint8_t _i2cAddress) {
    uint8_t rawData[] = {0x04, 0x13, 0x8B, 0x00, 0x01};

    // Take PPM
    Wire.beginTransmission(_i2cAddress);
    Wire.write(rawData, requestSize);
    Wire.endTransmission();

    delay(responseDelay);

    if (responseSize != Wire.requestFrom(_i2cAddress, responseSize)) {
        return 0;
    }

    for (uint8_t i = 0x00; responseSize > i; i++) {
        rawData[i] = Wire.read();
    }

    if (0x04 != rawData[fieldFunctionCode] || 0x02 != rawData[fieldBytesNumber]) {
        return 0;
    }

    return ((uint16_t) rawData[fieldDataMsb] << 8) | rawData[fieldDataLsb];
}

template <typename T>
int write_i2c (const T& value)
{
    const byte * p = (const byte*) &value;
    unsigned int i;
    for (i = 0; i < sizeof value; i++)
        Wire.write(*p++);
    return i;
}

void requestEvent() {
    // 16 byte for values
    write_i2c(t);
    write_i2c(h);
    write_i2c(hic);
    write_i2c(co2);
}

void receiveEvent(int size) {
    // 2 bytes because of some fucked shit in library and 1 for mode
    if( size != 3 ) return;

    byte buffer[size];
    Wire.readBytes(buffer, size);

    switch (buffer[2]) {
        case 0: // OFF
            screenStatus = SSD1306_DISPLAYOFF;
            break;
        case 1: // ON
            screenStatus = SSD1306_DISPLAYON;
            break;
        case 2: // TOGGLE
            screenStatus ^= SSD1306_DISPLAYOFF ^ SSD1306_DISPLAYON;
            break;
        default: // IDK
            reset();
            break;
    }

    statusChanged = true;
}

void setup() {
    display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
    Wire.begin(CURR_ADDRESS);
    Wire.onRequest(requestEvent);
    Wire.onReceive(receiveEvent);

    dht.begin();

    display.clearDisplay();

    display.setTextSize(2);
    display.setTextColor(WHITE);

    display.setCursor(0, 5);
    // Display static text
    display.println("Hello, ");
    display.println("    mate!");
    display.display();
}

void loop() {
    delay(DELAY);
    display.clearDisplay();

    co2 = getT67XXMetric(address);

    h = dht.readHumidity();
    t = dht.readTemperature();
    hic = dht.computeHeatIndex(t, h, false);

    display.setCursor(0, 5);
    display.println("T:  " + String(t) + "C");
    display.println("Hum:" + String(h) + "%");
    display.println("CO2:" + String(int(co2)));

    display.display();

    if( statusChanged )
    {
        statusChanged = false;
        display.ssd1306_command(screenStatus);
    }
}
