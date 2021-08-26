#include "Arduino.h"
#include "Wire.h"
#include "FastLED.h"

#include "Zone.hpp"

#define LED_NUM 250
#define LED_TYPE WS2812B
#define COLOR_ORDER GRB
#define DATA_PIN 7
#define LED_PIN 13
#define ZONE_NUM 10

#define ADDRESS 0x69

//==================

#define MSG_GEN 0
#define MSG_COL 1
#define MSG_MOD 2

#define COL_STATIC 0
#define COL_GRAD 1
#define COL_RND 2

#define STATE_STATIC 0
#define STATE_SNAKE 1

CRGB leds[LED_NUM];
Zone zones[ZONE_NUM];

// Board reset
void (* reset)() = nullptr;

/*
 * Read
 * 1. Incoming msg size
 * 2. Main header (zone number, msg_type)
 * 3. Msg type
 * 4. Mode for specific msg
 * 5. Msg body
 */
void handleData(byte* buffer)
{
    Zone& zone = zones[buffer[1]];
    switch (buffer[0])
    {
        case MSG_GEN:
        {
            // if zone start == end == 0, skip setting zone start or end
            if( buffer[2] || buffer[3] || buffer[4] || buffer [5] )
                zone.setRange((uint16_t)buffer[3] | ((uint16_t)buffer[2] << 8),
                              (uint16_t)buffer[5] | ((uint16_t)buffer[4]) << 8);
            zone.brightness = buffer[6];
            break;
        }

        case MSG_COL:
            switch (buffer[2]) // read color mode
            {
                case COL_STATIC:
                    zone.colorMode = ColorMode::COL;
                    zone.color = CRGB{buffer[3],  // R
                                      buffer[4],  // G
                                      buffer[5]}; // B
                    break;

                case COL_GRAD:
                {
                    zone.colorMode = ColorMode::GRAD;
                    zone.blending = static_cast<TBlendType>(buffer[3]);
                    zone.gradientSpeed = buffer[4];
                    zone.gradientColorStep = buffer[5];
                    byte bytes[buffer[6] * 4]; // 4 for RGB + index

                    for (byte i = 0; i < buffer[6]; ++i)
                    {
                        byte offset = 3 * i;

                        bytes[offset] = floor(255 / double(buffer[6] - 1)) * i; // color index in a palette

                        bytes[1 + offset] = buffer[7 + offset];     // R
                        bytes[2 + offset] = buffer[7 + offset + 1]; // G
                        bytes[3 + offset] = buffer[7 + offset + 2]; // B

                        zone.palette.loadDynamicGradientPalette(bytes);
                    }
                    break;
                }
                case COL_RND:
                    zone.colorMode = ColorMode::RND;
                    zone.delay = buffer[3];
                    break;

                default:
                    reset();
                    break;
            }
            break;

        case MSG_MOD:
            switch (buffer[2]) // read state mode
            {
                case STATE_STATIC:
                    zone.mode = Mode::STATIC;
                    break;

                case STATE_SNAKE:
                    zone.mode = Mode::SNAKE;
                    zone.direction = (int)buffer[3];

                    zone.sStart = zone.start; // TODO can be improved to look prettier
                    zone.sEnd = zone.start + ((uint16_t)buffer[5] | ((uint16_t)buffer[4] << 8));

                    zone.loop = buffer[6]; // TODO fix if end < start
                    zone.delay = buffer[7];
                    break;

                default:
                    reset();
                    break;
            }
            break;

        default:
            reset();
            break;
    }
}

void receiveEvent(int size) {
    digitalWrite(LED_PIN, HIGH);

    byte buffer[size + 2];
    Wire.readBytes(buffer, size);
    handleData(buffer + 2);

    digitalWrite(LED_PIN, LOW);
}


void setup() {

    Wire.begin(ADDRESS);
    Wire.onReceive(receiveEvent);

    pinMode(LED_PIN, OUTPUT);

    CFastLED::addLeds<LED_TYPE, DATA_PIN, COLOR_ORDER>(leds, LED_NUM).setCorrection(TypicalLEDStrip);

    //======

    zones[0].start = 0;
    zones[0].end = LED_NUM;

    zones[0].colorMode = ColorMode::GRAD;
    zones[0].gradientSpeed = 1;
    zones[0].mode = Mode::STATIC;
    zones[0].color = CRGB::Magenta;
    zones[0].loop = true;
    zones[0].brightness = 100;
    zones[0].sStart = 40;
    zones[0].sEnd = 100;
}

void loop()
{
    // clear causes flickering
    for(auto & i : zones)
        i.update();

    FastLED.show();
}
