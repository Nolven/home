#include "Arduino.h"
#include "Wire.h"
#include "FastLED.h"

#include "Zone.hpp"

#define LED_NUM 400
#define LED_TYPE WS2812B
#define COLOR_ORDER GRB
#define DATA_PIN 7
#define LED_PIN 13
#define ZONE_NUM 5

#define ADDRESS 0x6a
// 6a room
// 69 hallway

//==================

#define MSG_GEN 0
#define MSG_COL 1
#define MSG_MOD 2

CRGB leds[LED_NUM];
Zone zones[ZONE_NUM];

// Board reset
void (* reset)() = nullptr;

/*
 * Read
 * 1. Incoming msg size
 * 2. Main header (zone number, msg_type)
 * 3. Msg type
 * 4. DisplayMode for specific msg
 * 5. Msg body
 */
void handleData(byte* buffer)
{
    if( buffer[1] >= ZONE_NUM ) return; // a bit of protection
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
            switch (static_cast<ColorMode>(buffer[2])) // read color mode
            {
                case ColorMode::STATIC:
                    zone.colorMode = ColorMode::STATIC;
                    zone.color = CRGB{buffer[3],  // R
                                      buffer[4],  // G
                                      buffer[5]}; // B
                    break;

                case ColorMode::GRAD:
                {
                    zone.colorMode = ColorMode::GRAD;
                    zone.blending = static_cast<TBlendType>(buffer[3]);
                    zone.gradientSpeed = buffer[4];
                    zone.gradientColorStep = buffer[5];

                    byte colorLength = floor(PALETTE_SIZE / buffer[6]); // Currently, 16palette is used (5)

                    for (byte i = 0; i < buffer[6] - 1; ++i)
                    {
                        byte colorOffset = 3 * i;
                        fill_gradient_RGB(zone.palette,
                                          colorLength * i, CRGB(buffer[7 + colorOffset], buffer[8 + colorOffset], buffer[9 + colorOffset]),
                                          colorLength * (i + 1), CRGB(buffer[7 + colorOffset + 3], buffer[8 + colorOffset + 3], buffer[9 + colorOffset + 3]));
                    }

                    // last-first color gradient
                    fill_gradient_RGB(zone.palette,
                                      PALETTE_SIZE - colorLength, CRGB(buffer[7 + buffer[6] + 3], buffer[8 + buffer[6] + 3], buffer[9 + buffer[6] + 3]), // first color
                                      PALETTE_SIZE, CRGB(buffer[7], buffer[8], buffer[9]));

                    break;
                }
                case ColorMode::RND:
                    zone.colorMode = ColorMode::RND;
                    zone.delay = buffer[3];
                    break;

                default:
                    reset();
                    break;
            }
            break;

        case MSG_MOD:
            switch (static_cast<DisplayMode>(buffer[2])) // read state mode
            {
                case DisplayMode::STATIC:
                    zone.mode = DisplayMode::STATIC;
                    break;

                case DisplayMode::SNAKE:
                    zone.mode = DisplayMode::SNAKE;
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
    zones[0].mode = DisplayMode::STATIC;
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
