#ifndef HOMELIGHTS_ZONE_H
#define HOMELIGHTS_ZONE_H

#include <FastLED.h>

enum Mode
{
    FLAT,
    SNAKE,
};

enum ColorMode
{
    RND,
    COL,
    GRAD
};

struct Zone {
    Zone() = default;

    void paint(unsigned s, unsigned e) const {
        for (; s < e; ++s) {
            switch (colorMode) {
                case RND:
                    FastLED.leds()[s] = CHSV(random(), 255, 255);
                    break;

                case COL:
                    FastLED.leds()[s] = color;
                    break;

                case GRAD:
                    //TODO
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

    void nonLoopSnake()
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

        switch (mode)
        {
            case FLAT:
                paint(start, end);
                break;

            case SNAKE:
                if (loop)
                    loopSnake();
                else
                    nonLoopSnake();
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

    //Snake
    bool loop = true;
    int direction = 1; // and also speed
    unsigned sStart{};
    unsigned sEnd{};

    CRGB color = CRGB::DarkGray;

    ColorMode colorMode = ColorMode::COL;
    Mode mode = Mode::SNAKE;
};

#endif