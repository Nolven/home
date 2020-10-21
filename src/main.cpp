#include "Arduino.h"
#include "FastLED.h"
#include "Zone.h"

#define DATA_PIN 3
#define LED_TYPE WS2812B
#define COLOR_ORDER GRB
#define NUM_LEDS 150
#define BRIGHTNESS 96

CRGB leds[NUM_LEDS];
Zone zone[50];


void setup() {
    delay(3000); // initial delay of a few seconds is recommended
    CFastLED::addLeds<LED_TYPE,DATA_PIN,COLOR_ORDER>(leds, NUM_LEDS).setCorrection(TypicalLEDStrip); // initializes LED strip

    zone[0].sStart = 12;
    zone[0].sEnd = 18;

    zone[0].start = 10;
    zone[0].end = 20;
}

// main program
void loop()
{
    //Color
    zone[0].colorMode = COL;
    zone[0].mode = FLAT;

    int i = 100;
    while ( i-- )
    {
        zone[0].update();
        FastLED.show();
        --zone[0].brightness;
        delay(100);
    }




    /*int x = 20;
    while (--x)
    {
        zone[0].shift(1);
        zone[0].update();
        FastLED.show();
        delay(150);
    }

    while (++x < 20)
    {
        zone[0].shift(-1);
        zone[0].update();
        FastLED.show();
        delay(150);
    }*/

    /*for (int i = 0; i < NUM_LEDS - 1; ++i)
    {
        if( i )
            leds[i-1] = CRGB::Black;
        leds[i] = CRGB::AntiqueWhite;
        FastLED.show();
        delay(15);
    }*/
}