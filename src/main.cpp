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
    FastLED.addLeds<LED_TYPE,DATA_PIN,COLOR_ORDER>(leds, NUM_LEDS).setCorrection(TypicalLEDStrip); // initializes LED strip

    zone[0].start = 10;
    zone[0].end = 20;

}

// main program
void loop()
{
    zone[0].show();
    FastLED.show();


    int x = 5;
    while (--x)
    {
        zone[0].changeEnd(5);
        zone[0].show();
        FastLED.show();
        delay(1500);
    }

    while (++x < 5)
    {
        zone[0].changeEnd(-5);
        zone[0].show();
        FastLED.show();
        delay(1500);
    }

    /*for (int i = 0; i < NUM_LEDS - 1; ++i)
    {
        if( i )
            leds[i-1] = CRGB::Black;
        leds[i] = CRGB::AntiqueWhite;
        FastLED.show();
        delay(15);
    }*/
}