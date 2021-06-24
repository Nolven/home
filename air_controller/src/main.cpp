#include <Wire.h>
#include <Arduino.h>
#include "DHT.h"

// DHT
#define DHTPIN 7
#define DHTTYPE DHT11   // DHT 11

// t6703
constexpr byte address      = 0x15;
constexpr byte requestSize  = 0x05;
constexpr byte responseSize = 0x04;
constexpr byte responseDelay = 0x0A;
constexpr byte fieldFunctionCode =  0x00;
constexpr byte fieldBytesNumber  =  0x01;
constexpr byte fieldDataMsb = 0x02;
constexpr byte fieldDataLsb = 0x03;

constexpr auto baudRate = 115200;

// Common
#define DELAY 5000
#define ADDRESS 0x22 // this board

// buffers
float h;
float t;
float hic;
float co2;

DHT dht(DHTPIN, DHTTYPE);

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

template <typename T> int write_i2c (const T& value)
{
    const byte * p = (const byte*) &value;
    unsigned int i;
    for (i = 0; i < sizeof value; i++)
        Wire.write(*p++);
    return i;
}  // end of I2C_writeAnything

void requestEvent() {
    // 16 byte for values
    write_i2c(t);
    write_i2c(h);
    write_i2c(hic);
    write_i2c(co2);
}

void setup() {
    Wire.begin(ADDRESS);
    Wire.onRequest(requestEvent);

    Serial.begin(baudRate);

    dht.begin();
}

void loop()
{
    delay(DELAY);

    co2 = getT67XXMetric(address);

    h = dht.readHumidity();
    t = dht.readTemperature();
    hic = dht.computeHeatIndex(t, h, false);
}
