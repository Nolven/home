#ifndef HOMELIGHTS_ZONE_H
#define HOMELIGHTS_ZONE_H

#include <FastLED.h>

struct Zone
{
    Zone() = default;
    Zone(int s, int e) : start(s), end(e)
    {};

    void show()
    {
        for( int i = start; i < end; ++i)
            FastLED.leds()[i] = CRGB::Green;
    }

    void shift(int n)
    {
        if (n < 0)
            for( int i = end ; i < end - n; ++i )
                FastLED.leds()[i] = CRGB::Black;
        else
            for( int i = start; i < start + n; ++i )
                FastLED.leds()[i] = CRGB::Black;

        start += n;
        end += n;
    }

    void changeEnd(int n)
    {
        if( n < 0 )
        {
            for( int i = end - n; i < end; ++i )
                FastLED.leds()[i] = CRGB::Black;
        }
        end += n;
    }

    void changeStart(int n)
    {
        if( n > 0 )
        {
            end += n;
        } else
        {
            for( int i = end - n; i < end; ++i )
                FastLED.leds()[i] = CRGB::Black;
        }
    }

    int start = 0;
    int end = 0;
};

#endif