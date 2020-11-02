#include "Arduino.h"
#include "FastLED.h"
#include "Zone.h"

#define DATA_PIN 3
#define LED_TYPE WS2812B
#define COLOR_ORDER GRB
#define NUM_LEDS 150
#define BRIGHTNESS 96

CRGB leds[NUM_LEDS];

#define ZONE_NUM 10
Zone zone[ZONE_NUM];


void setup() {
    delay(2000); // initial delay of a few seconds is recommended
    CFastLED::addLeds<LED_TYPE,DATA_PIN,COLOR_ORDER>(leds, NUM_LEDS).setCorrection(TypicalLEDStrip); // initializes LED strip

    //======

    zone[0].start = 0;
    zone[0].end = 150;

    zone[0].colorMode = ColorMode::COL;
    zone[0].mode = Mode::STATIC;
    zone[0].color = CRGB::OrangeRed;
    zone[0].loop = true;
    zone[0].brightness = 100;
}

bool isZoneIntersection(const Zone& a, int change, const Zone& b) {
    // if zone start\end + change is in range of other zone
    if ( ((a.start + change) > b.start && (a.start + change) < b.end)
        || ((a.end + change) > b.start && (a.end + change) < b.end) )
    {
        return true;
    }
    return false;
}

bool moveZone( unsigned index, int change )
{
    bool needShift = true;
    for( byte i = 0; i < ZONE_NUM; ++i )
    {
        if( i == index )
            continue;

        if( zone[i].start != zone[i].end && isZoneIntersection(zone[index], change, zone[i]) )
        {
            //Increase zone to the size of intersected zone
            if( zone[i].start < zone[index].start )
                zone[index].start = zone[i].start;

            if( zone[i].end > zone[index].end )
                zone[index].end = zone[i].end;

            //Nullify other zone
            zone[i].start = zone[i].end;

            needShift = false;
        }
    }

    return needShift;
}

enum serialHeaders : byte
{
    BASIC_MSG,
    TOGGLE_MSG
};

void readZone(const byte* data)
{
    bool ignoreZeros = data[0] & 128;
    byte index = data[1];

}

void readSerial()
{
    byte data[64];
    while( Serial.available() )
    {
        Serial.readBytes(data, 64);
        switch ( static_cast<serialHeaders>(data[0] & 127) ) {

            case BASIC_MSG:
                readZone(&data[0]);
                readZone(&data[16]);
                readZone(&data[32]);
                readZone(&data[48]);
                break;
            case TOGGLE_MSG:
                break;
        }
    }
}

// main program
void loop()
{
    readSerial();
    for(auto & i : zone)
        i.update();

    FastLED.show();
}