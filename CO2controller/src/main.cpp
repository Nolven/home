#include <Wire.h>
#include <Arduino.h>

constexpr byte address      = 0x15;
constexpr byte requestSize  = 0x05;
constexpr byte responseSize = 0x04;
constexpr byte responseDelay = 0x0A;
constexpr byte fieldFunctionCode =  0x00;
constexpr byte fieldBytesNumber  =  0x01;
constexpr byte fieldDataMsb = 0x02;
constexpr byte fieldDataLsb = 0x03;

constexpr auto baudRate = 115200;


// t6703-5k
uint16_t getT67XXMetric(uint8_t _i2cAddress, uint16_t& _value) {
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


void setup() {
    Wire.begin();
    Serial.begin(baudRate);
}

void loop()
{
    uint16_t co2ppm = getT67XXMetric(address, co2ppm);
    if (co2ppm) {
        Serial.println(co2ppm);
    } else {
        Serial.println("Sensor failure");
    }
    delay(5000);
}