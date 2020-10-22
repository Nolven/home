#ifndef HOME_LIGHT_GRADIENT_H
#define HOME_LIGHT_GRADIENT_H

#define EPSILON 0.001f

struct buffCol
{
    double r;
    double g;
    double b;
};

buffCol norm(const CRGB& col)
{
    return {double (col.r) / 255, double (col.g) / 255, double (col.b) / 255};
}

double linInterp(double col1, double col2, double ratio)
{
    return col1 * (1 - ratio) + col2 * ratio;
}

void inverseCompanding(buffCol& col)
{
    if (col.r > 0.04045) col.r = pow((col.r+0.055)/1.055, 2.4); else col.r = col.r / 12.92;
    if (col.g > 0.04045) col.g = pow((col.g+0.055)/1.055, 2.4); else col.g = col.g / 12.92;
    if (col.b > 0.04045) col.b = pow((col.b+0.055)/1.055, 2.4); else col.b = col.b / 12.92;
}

void compandingColor(double& x)
{
    if (x > 0.0031308)
        x = 1.055*pow(x, 1/2.4)-0.055;
    else
        x *= 12.92;
}

CRGB companding(double& r, double& g, double& b)
{
    compandingColor(r);
    compandingColor(g);
    compandingColor(b);

    return {static_cast<uint8_t>(255u * r), static_cast<uint8_t>(255u * g), static_cast<uint8_t>(255u * b)};
}

CRGB gradient(const CRGB& incol, const CRGB& incol2, double ratio)
{
    double gamma = .43;

    buffCol col = norm(incol);
    buffCol col2 = norm(incol2);

    inverseCompanding(col);
    inverseCompanding(col2);

    double r = linInterp(col.r, col2.r, ratio);
    double g = linInterp(col.g, col2.g, ratio);
    double b = linInterp(col.b, col2.b, ratio);

    double b1 = pow(col.r + col.g + col.b, gamma);
    double b2 = pow(col2.r + col2.g + col2.b, gamma);

    double brightness = linInterp(b1, b2, ratio);
    double intensity = pow(brightness, 1/gamma);

    if( (r + g + b) > EPSILON )
    {
        double factor = intensity / (r + g + b);
        r = r * factor;
        g = g * factor;
        b = b * factor;
    }

    return companding(r, g, b);
}

//Lazy copy-paste
//#########################################################################

CRGB InverseSrgbCompanding(const CRGB& c)
{
    //Convert color from 0..255 to 0..1
    double r = double (c.r) / 255;
    double g = double (c.g) / 255;
    double b = double (c.b) / 255;

    //Inverse Red, Green, and Blue
    if (r > 0.04045) r = pow((r+0.055)/1.055, 2.4); else r = r / 12.92;
    if (g > 0.04045) g = pow((g+0.055)/1.055, 2.4); else g = g / 12.92;
    if (b > 0.04045) b = pow((b+0.055)/1.055, 2.4); else b = b / 12.92;

    //return new color. Convert 0..1 back into 0..255
    CRGB result;
    result.r = r*255;
    result.g = g*255;
    result.b = b*255;

    return result;
}

CRGB SrgbCompanding(const CRGB& c)
{
    //Convert color from 0..255 to 0..1
    double r = double(c.r) / 255;
    double g = double(c.g) / 255;
    double b = double(c.b) / 255;

    //Apply companding to Red, Green, and Blue
    if (r > 0.0031308) r = 1.055*pow(r, 1/2.4)-0.055; else r = r * 12.92;
    if (g > 0.0031308) g = 1.055*pow(g, 1/2.4)-0.055; else g = g * 12.92;
    if (b > 0.0031308) b = 1.055*pow(b, 1/2.4)-0.055; else b = b * 12.92;

    //return new color. Convert 0..1 back into 0..255
    CRGB result;
    result.r = r*255;
    result.g = g*255;
    result.b = b*255;

    return result;
}


//This is the wrong algorithm. Don't do this
CRGB ColorMix(CRGB c1, CRGB c2, double mix)
{
    //Mix [0..1]
    //  0   --> all c1
    //  0.5 --> equal mix of c1 and c2
    //  1   --> all c2

    //Invert sRGB gamma compression
    c1 = InverseSrgbCompanding(c1);
    c2 = InverseSrgbCompanding(c2);

    CRGB result;

    result.r = c1.r*(1-mix) + c2.r*(mix);
    result.g = c1.g*(1-mix) + c2.g*(mix);
    result.b = c1.b*(1-mix) + c2.b*(mix);

    //Reapply sRGB gamma compression
    result = SrgbCompanding(result);

    return result;
}

#endif //HOME_LIGHT_GRADIENT_H
