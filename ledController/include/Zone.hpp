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

    void paint(uint16_t s, uint16_t e) const
    {
        uint8_t index = paletteStart; //for grad
        for (; s < e; ++s) {
            switch (colorMode)
            {
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
                    FastLED.leds()[s] = ColorFromPalette(palette, index, brightness,  blending);
                    index += gradientColorStep;
                    break;
                }
                case MUSIC:
                    //TODO implement
                    break;
            }
        }
    }

    static inline void paintItBlack(uint16_t s, uint16_t e) {
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

        if(delayCounter++ < delay )
            return;
        else
            delayCounter = 0;

        if( colorMode == ColorMode::GRAD )
            paletteStart += gradientSpeed;

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

    void setStart(uint8_t newPos)
    {
        if( newPos > start )
            paintItBlack(start, newPos);
        start = newPos;
    }

    void setEnd(uint8_t newPos)
    {
        if( newPos < end )
            paintItBlack(newPos, end);
        end = newPos;
    }

    //Zone
    uint16_t start = 0; // smaller
    uint16_t end = 0; // greater

    uint8_t delay = 0; //cycles to skip
    uint8_t delayCounter = 0;

    //Snake
    bool loop = true;
    int8_t direction = 1;
    unsigned sStart = start;
    unsigned sEnd = end;
    // bool fade;
    // uint8_t fadePercentage;
    // Implement length

    //Color
    CRGB color = CRGB::LavenderBlush;
    byte brightness = 255;

    //Gradient
    CRGBPalette16 palette = LavaColors_p;
    TBlendType blending = TBlendType::LINEARBLEND;
    uint8_t paletteStart = 0;
    byte gradientSpeed = 0;
    byte gradientColorStep = 20;

    //Modes
    ColorMode colorMode = ColorMode::GRAD;
    Mode mode = Mode::STATIC;
};

#endif