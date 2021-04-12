#include "Arduino.h"
#include "FastLED.h"

#include "Zone.hpp"

#define LED_TYPE WS2812B
#define COLOR_ORDER GRB
constexpr size_t numberOfLeds = 150;
constexpr byte dataPin = 7;


CRGB leds[numberOfLeds];

constexpr uint8_t numberOfZones = 10;
Zone zones[numberOfZones];

void setup() {
    delay(2000); // initial delay of a few seconds is recommended
    CFastLED::addLeds<LED_TYPE,dataPin,COLOR_ORDER>(leds, numberOfLeds).setCorrection(TypicalLEDStrip); // initializes LED strip
    Serial.begin(2400);
    Serial.setTimeout(20);

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
 * 3. Msg type (general - 0, color - 1, mode - 2)
 * 4. Mode for specific msg (clr/grdnt/rnd for color, static/snake)
 * 5. Msg body
 */
void serialEvent() {
    static uint8_t nextMsgSize = 0; // 0 means that msg size is not received yet

    if (Serial.available())
    {
        if( !nextMsgSize )
            nextMsgSize = Serial.read(); // Msg size

        if( Serial.available() >= nextMsgSize )
        {
            Serial.write(Serial.available()); // Gonna use it as ack that msg been received

            byte buffer[nextMsgSize];
            Serial.readBytes(buffer, nextMsgSize);
            nextMsgSize = 0;

            byte zone = buffer[1];

            /**
             * Msg_types:
             * 0 - general msg consists of header
             * 1 - color mode
             * 2 - state mode
             */
            switch (buffer[0])
            {
                case 0:
                {
                    zones[zone].setRange((uint16_t)buffer[3] | ((uint16_t)buffer[2] << 8),
                                         (uint16_t)buffer[5] | ((uint16_t)buffer[4]) << 8);
                    zones[zone].brightness = buffer[6];
                    break;
                }

                case 1:
                    /**
                     * Color modes:
                     * 0 - static
                     * 1 - gradient
                     * 2 - random
                     */
                    switch (buffer[2]) // read color mode
                    {
                        case 0:
                            zones[zone].colorMode = ColorMode::COL;
                            zones[zone].color = CRGB{buffer[3],  // R
                                                     buffer[4],  // G
                                                     buffer[5]}; // B
                            break;

                        case 1:
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
                        case 2:
                            zones[zone].colorMode = ColorMode::RND;
                            zones[zone].delay = buffer[3];
                            break;

                        default:
                            Serial.write(-1);
                            break;
                    }
                    break;

                case 2:
                 /** Show mode:
                  * 0 - static
                  * 1 - snake
                  */
                    switch (buffer[2]) // read state mode
                    {
                        case 0:
                            zones[zone].mode = Mode::STATIC;
                            break;

                        case 1:
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
                    Serial.write(-1); // TODO error code
                    break;
            }
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
