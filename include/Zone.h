#ifndef HOMELIGHTS_ZONE_H
#define HOMELIGHTS_ZONE_H

#include <FastLED.h>

enum Mode
{
    STATIC,
    SNAKE,
};

enum ColorMode
{
    RND,
    COL,
    GRAD,
    MUSIC
};

struct Zone {
    Zone() = default;

    void paint(unsigned s, unsigned e) const
    {
        double steps = e - s;
        for (; s < e; ++s) {
            switch (colorMode) {
                case RND:
                    FastLED.leds()[s] = CHSV(random(), 255, brightness);
                    break;

                case COL:
                {
                    double b = double(brightness) / 255;
                    FastLED.leds()[s] = CRGB(color.r*b, color.g*b, color.b*b);
                    break;
                }
                case GRAD:
                {
                    static int index = 0;
                    FastLED.leds()[s] = ColorFromPalette(palette, index, brightness,  blending);
                    index += 3;
                    break;
                }
                case MUSIC:
                    //TODO implement
                    break;
            }
        }
    }

    static inline void paintItBlack(unsigned s, unsigned e) {
        for (; s < e; ++s) {
            FastLED.leds()[s] = CRGB::Black;
        }
    }

    void loopSnake()
    {
        if ( sStart > sEnd )
        {
            paint(sStart, end);
            paint(start, sEnd);
            paintItBlack(sEnd, sStart);
        } else
        {
            paint(sStart, sEnd);
            paintItBlack(start, sStart); //TODO optimization required
            paintItBlack(sEnd, end);
        }

        //I don't like this
        if( direction > 0 )
        {
            if ( sStart == end )
                sStart = start;

            if ( sEnd == end )
                sEnd = start;
        } else {
            if ( sStart == start )
                sStart = end;

            if ( sEnd == start )
                sEnd = end;
        }

        sEnd += direction;
        sStart += direction;
    }

    void bouncySnake()
    {
        paintItBlack(start, sStart); //TODO optimization required
        paintItBlack(sEnd, end);

        paint(sStart, sEnd);

        if( sStart == start || sEnd == end )
        {
            direction *= (-1);
        }

        sStart += direction;
        sEnd += direction;
    }

    void update()
    {
        if (start == end)
            return;

        if( counter++ < delay )
            return;
        else
            counter = 0;

        switch (mode)
        {
            case STATIC:
                paint(start, end);
                break;

            case SNAKE:
                if (loop)
                    loopSnake();
                else
                    bouncySnake();
                break;
        }
    }

    void shift(int n)
    {
        moveStart(n);
        moveEnd(n);
    }

    void moveEnd(int n)
    {
        if( n < 0 )
        {
            for( unsigned i = end + n; i < end; ++i )
                FastLED.leds()[i] = CRGB::Black;
        }
        end += n;
    }

    void moveStart(int n)
    {
        if( n > 0 )
        {
            for( unsigned i = start; i < start + n; ++i )
                FastLED.leds()[i] = CRGB::Black;
        }
        start += n;
    }

    //Zone
    unsigned start = 0; // smaller
    unsigned end = 0; // greater

    unsigned delay = 0; //cycles to skip
    unsigned counter = 0;

    //Snake
    bool loop = true;
    int direction = 1; // and also speed
    unsigned sStart = start;
    unsigned sEnd = end;

    //Color
    CRGB color = CRGB::LavenderBlush;
    byte brightness = 255;

    //Gradient
    CRGBPalette16 palette = RainbowColors_p;
    TBlendType blending = LINEARBLEND;
    
    //Modes
    ColorMode colorMode = ColorMode::RND;
    Mode mode = Mode::STATIC;
};

#endif