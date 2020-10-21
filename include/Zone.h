#ifndef HOMELIGHTS_ZONE_H
#define HOMELIGHTS_ZONE_H

#include <FastLED.h>

enum Mode
{
    COL,
    RND,
    EPI,
    SNAKE,
    GRAD,
    BLINK
};

struct Zone
{
    Zone() = default;
    Zone(int s, int e) : start(s), end(e)
    {};

    void update()
    {
        switch (mode)
        {
            case COL:
                for( int i = start; i < end; ++i)
                    FastLED.leds()[i] = color;
                break;
            case EPI:
                if (!is)
                {
                    is = true;
                    for( int i = start; i < end; ++i)
                        FastLED.leds()[i] = CRGB::Black;
                }
                else
                {
                    for( int i = start; i < end; ++i)
                        FastLED.leds()[i] = CHSV(random(), 255, 255);
                    is = false;
                }
                break;
            case RND:
                for( int i = start; i < end; ++i)
                    FastLED.leds()[i] = CHSV(random(), 255, 255);
                break;
            case SNAKE:
                break;
            case GRAD:
                //TODO
                break;
            case BLINK:
                //TODO
                break;
        }

    }

    void shift(int n)
    {
        changeStart(n);
        changeEnd(n);
    }

    void changeEnd(int n)
    {
        if( n < 0 )
        {
            for( int i = end + n; i < end; ++i )
                FastLED.leds()[i] = CRGB::Black;
        }
        end += n;
    }

    void changeStart(int n)
    {
        if( n > 0 )
        {
            for( int i = start; i < start + n; ++i )
                FastLED.leds()[i] = CRGB::Black;
        }
        start += n;
    }

    //Epi
    bool is = true;

    //Snake
    bool loop = false;
    unsigned sStart;
    unsigned sEnd;
    unsigned length;

    //Zone
    int start = 0;
    int end = 0;

    CRGB color = CRGB::Green;
    Mode mode = Mode::EPI;
};

#endif