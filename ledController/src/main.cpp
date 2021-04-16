#include "Arduino.h"
#include "FastLED.h"

#include "Zone.hpp"

#define LED_TYPE WS2812B
#define COLOR_ORDER GRB

//it's good to have some excess memory
namespace Contracts{
    // Msg type
    constexpr byte generalMsg = 0;
    constexpr byte colorMsg = 1;
    constexpr byte modeMsg = 2;

    // Color mode
    constexpr byte staticColor = 0;
    constexpr byte gradColor = 1;
    constexpr byte rndColor = 2;

    //State ?mode?
    constexpr byte staticState = 0;
    constexpr byte snakeState = 1;
}

namespace Configs{
    // Serial
    constexpr uint16_t baudRate = 2400;
    constexpr byte timeout = 20;

    // Led
    constexpr size_t initDelay = 2000;
    constexpr size_t numberOfLeds = 150;
    constexpr byte dataPin = 7;
    constexpr byte zonesNumber = 10;
}

CRGB leds[Configs::numberOfLeds];
Zone zones[Configs::zonesNumber];

// Board reset
void (* reset)() = nullptr;

void setup() {
    delay(Configs::initDelay); // initial delay of a few seconds is recommended
    CFastLED::addLeds<LED_TYPE,Configs::dataPin,COLOR_ORDER>(leds, Configs::numberOfLeds).setCorrection(TypicalLEDStrip); // initializes LED strip
    Serial.begin(Configs::baudRate);
    Serial.setTimeout(Configs::timeout);

    //======

    zones[0].start = 0;
    zones[0].end = 150;

    zones[0].colorMode = ColorMode::GRAD;
    zones[0].gradientSpeed = 1;
    zones[0].mode = Mode::STATIC;
    zones[0].color = CRGB::Magenta;
    zones[0].loop = true;
    zones[0].brightness = 100;
    zones[0].sStart = 40;
    zones[0].sEnd = 100;
}

/*
 * Read
 * 1. Incoming msg size
 * 2. Main header (zone number, msg_type)
 * 3. Msg type
 * 4. Mode for specific msg
 * 5. Msg body
 */
void serialEvent() {
    static uint8_t nextMsgSize = 0; // 0 means that msg size is not received yet

    if (Serial.available())
    {
        // Receive incoming msg size
        if( !nextMsgSize )
        {
            nextMsgSize = Serial.read();
            if( !nextMsgSize ) reset(); // 0 is for board reset
        }

        if( Serial.available() >= nextMsgSize )
        {
            byte buffer[nextMsgSize];
            Serial.readBytes(buffer, nextMsgSize);
            nextMsgSize = 0;

            byte zone = buffer[1];

            switch (buffer[0])
            {
                case Contracts::generalMsg:
                {
                    // if zone start == end == 0, skip setting zone start or end
                    if( buffer[2] || buffer[3] || buffer[4] || buffer [5] )
                        zones[zone].setRange((uint16_t)buffer[3] | ((uint16_t)buffer[2] << 8),
                                            (uint16_t)buffer[5] | ((uint16_t)buffer[4]) << 8);
                    zones[zone].brightness = buffer[6];
                    break;
                }

                case Contracts::colorMsg:
                    switch (buffer[2]) // read color mode
                    {
                        case Contracts::staticColor:
                            zones[zone].colorMode = ColorMode::COL;
                            zones[zone].color = CRGB{buffer[3],  // R
                                                     buffer[4],  // G
                                                     buffer[5]}; // B
                            break;

                        case Contracts::gradColor:
                        {
                            zones[zone].colorMode = ColorMode::GRAD;
                            zones[zone].blending = static_cast<TBlendType>(buffer[3]);
                            zones[zone].gradientSpeed = buffer[4];
                            zones[zone].gradientColorStep = buffer[5];
                            byte bytes[buffer[6] * 4]; // 4 for RGB + index

                            for (byte i = 0; i < buffer[6]; ++i)
                            {
                                byte offset = 3 * i;

                                bytes[offset] = floor(255 / double(buffer[6] - 1)) * i; // color index in a palette

                                bytes[1 + offset] = buffer[7 + offset];     // R
                                bytes[2 + offset] = buffer[7 + offset + 1]; // G
                                bytes[3 + offset] = buffer[7 + offset + 2]; // B

                                zones[zone].palette.loadDynamicGradientPalette(bytes);
                            }
                            break;
                        }
                        case Contracts::rndColor:
                            zones[zone].colorMode = ColorMode::RND;
                            zones[zone].delay = buffer[3];
                            break;

                        default:
                            reset();
                            break;
                    }
                    break;

                case Contracts::modeMsg:
                    switch (buffer[2]) // read state mode
                    {
                        case Contracts::staticState:
                            zones[zone].mode = Mode::STATIC;
                            break;

                        case Contracts::snakeState:
                            zones[zone].mode = Mode::SNAKE;
                            zones[zone].direction = (int)buffer[3];

                            zones[zone].sStart = zones[zone].start; // TODO can be improved to look prettier
                            zones[zone].sEnd = zones[zone].start + ((uint16_t)buffer[5] | ((uint16_t)buffer[4] << 8));

                            zones[zone].loop = buffer[6]; // TODO fix if end < start
                            zones[zone].delay = buffer[7];
                            break;

                        default:
                            Serial.write(-1);
                            break;
                    }
                    break;

                default:
                    reset();
                    break;
            }

            // Ack to the received msg
            Serial.write(Serial.available());
        }
    }
}

void loop()
{
    // no clear because it causes flickering
    for(auto & i : zones)
        i.update();

    FastLED.show();
}
