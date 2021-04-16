#ifndef CO2SENSOR_SCREENCONTROLLER_H
#define CO2SENSOR_SCREENCONTROLLER_H

#include "Arduino.h"

// For 7-segment displays based 74НС595 micro
class ScreenController
{
public:
    // Opens pins
    // Not constructor cause class would probably be in the global scope before Setup()
    void open(byte latchPin, byte dataPin, byte clockPin){
        _latch = latchPin;
        _data = dataPin;
        _clock = clockPin;

        pinMode(_latch, OUTPUT);
        pinMode(_clock, OUTPUT);
        pinMode(_data, OUTPUT);
    }

    // Displays data on the screen
    void update() const{
        digitalWrite(_latch, LOW);

        if( _nextDigit == 8 ) _nextDigit = 0;  //cause of the clock we can update only 1 symbol per loop

        if( data[_nextDigit] >= 0 && data[_nextDigit] < 10 ) //if it's not in the range, we just skip it
        {
            shiftOut(_data, _clock, MSBFIRST, _numbers[data[_nextDigit]]);  // Write number
            shiftOut(_data, _clock, MSBFIRST, _digits[_nextDigit]);         // Enable digit
        }

        digitalWrite(_latch, HIGH);
        ++_nextDigit;
    }

private:
    byte _latch{};
    byte _data{};
    byte _clock{};

    mutable byte _nextDigit = 0;

    static constexpr byte digitsNumber = 8;
    //   Digits                    0     1     2     3     4     5     6     7
    byte _digits[digitsNumber] = {0x10, 0x20, 0x40, 0x80, 0x01, 0x02, 0x04, 0x08};

    //   Numbers          0     1     2     3     4     5     6     7     8     9
    byte _numbers[10] = {0xC0, 0xF9, 0xA4, 0xB0, 0x99, 0x92, 0x82, 0xF8, 0x80, 0x90};


    byte pointDigit = 0x80;

public:
    // What will be written this iteration
    byte data[digitsNumber] = {0,10,20,0,10,10,10,7};
};

#endif //CO2SENSOR_SCREENCONTROLLER_H
