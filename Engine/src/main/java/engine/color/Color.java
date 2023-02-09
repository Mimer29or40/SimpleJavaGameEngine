package engine.color;

import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

public class Color implements Colorc, Externalizable, Cloneable
{
    public static final Colorc WHITE      = new Color(255);
    public static final Colorc LIGHT_GRAY = new Color(191);
    public static final Colorc GRAY       = new Color(127);
    public static final Colorc DARK_GRAY  = new Color(63);
    public static final Colorc BLACK      = new Color(0);
    
    public static final Colorc LIGHT_GREY = LIGHT_GRAY;
    public static final Colorc GREY       = GRAY;
    public static final Colorc DARK_GREY  = DARK_GRAY;
    
    public static final Colorc LIGHTEST_RED = new Color(255, 191, 191);
    public static final Colorc LIGHTER_RED  = new Color(255, 127, 127);
    public static final Colorc LIGHT_RED    = new Color(255, 63, 63);
    public static final Colorc RED          = new Color(255, 0, 0);
    public static final Colorc DARK_RED     = new Color(191, 0, 0);
    public static final Colorc DARKER_RED   = new Color(127, 0, 0);
    public static final Colorc DARKEST_RED  = new Color(63, 0, 0);
    
    public static final Colorc LIGHTEST_YELLOW = new Color(255, 255, 191);
    public static final Colorc LIGHTER_YELLOW  = new Color(255, 255, 127);
    public static final Colorc LIGHT_YELLOW    = new Color(255, 255, 63);
    public static final Colorc YELLOW          = new Color(255, 255, 0);
    public static final Colorc DARK_YELLOW     = new Color(191, 191, 0);
    public static final Colorc DARKER_YELLOW   = new Color(127, 127, 0);
    public static final Colorc DARKEST_YELLOW  = new Color(63, 63, 0);
    
    public static final Colorc LIGHTEST_GREEN = new Color(191, 255, 191);
    public static final Colorc LIGHTER_GREEN  = new Color(127, 255, 127);
    public static final Colorc LIGHT_GREEN    = new Color(63, 255, 63);
    public static final Colorc GREEN          = new Color(0, 255, 0);
    public static final Colorc DARK_GREEN     = new Color(0, 191, 0);
    public static final Colorc DARKER_GREEN   = new Color(0, 127, 0);
    public static final Colorc DARKEST_GREEN  = new Color(0, 63, 0);
    
    public static final Colorc LIGHTEST_CYAN = new Color(191, 255, 255);
    public static final Colorc LIGHTER_CYAN  = new Color(127, 255, 255);
    public static final Colorc LIGHT_CYAN    = new Color(63, 255, 255);
    public static final Colorc CYAN          = new Color(0, 255, 255);
    public static final Colorc DARK_CYAN     = new Color(0, 191, 191);
    public static final Colorc DARKER_CYAN   = new Color(0, 127, 127);
    public static final Colorc DARKEST_CYAN  = new Color(0, 63, 63);
    
    public static final Colorc LIGHTEST_BLUE = new Color(191, 191, 255);
    public static final Colorc LIGHTER_BLUE  = new Color(127, 127, 255);
    public static final Colorc LIGHT_BLUE    = new Color(63, 63, 255);
    public static final Colorc BLUE          = new Color(0, 0, 255);
    public static final Colorc DARK_BLUE     = new Color(0, 0, 191);
    public static final Colorc DARKER_BLUE   = new Color(0, 0, 127);
    public static final Colorc DARKEST_BLUE  = new Color(0, 0, 63);
    
    public static final Colorc LIGHTEST_MAGENTA = new Color(255, 191, 255);
    public static final Colorc LIGHTER_MAGENTA  = new Color(255, 127, 255);
    public static final Colorc LIGHT_MAGENTA    = new Color(255, 63, 255);
    public static final Colorc MAGENTA          = new Color(255, 0, 255);
    public static final Colorc DARK_MAGENTA     = new Color(191, 0, 191);
    public static final Colorc DARKER_MAGENTA   = new Color(127, 0, 127);
    public static final Colorc DARKEST_MAGENTA  = new Color(63, 0, 63);
    
    public static final Colorc BLANK = new Color(0, 0);
    
    // 0.299R + 0.587G + 0.114B
    private static final double R_TO_GRAY = 0.299;
    private static final double G_TO_GRAY = 0.587;
    private static final double B_TO_GRAY = 0.114;
    
    public static int toGray(int r, int g, int b)
    {
        return toInt((r * R_TO_GRAY + g * G_TO_GRAY + b * B_TO_GRAY) / 255.0);
    }
    
    public static double toGray(double r, double g, double b)
    {
        return toFloat(r * R_TO_GRAY + g * G_TO_GRAY + b * B_TO_GRAY);
    }
    
    public static int toInt(byte value)
    {
        return value & 0xFF;
    }
    
    public static int toInt(int value)
    {
        return value < 0 ? 0 : Math.min(value, 255);
    }
    
    public static int toInt(double value)
    {
        return toInt((int) Math.floor(value * 256));
    }
    
    public static double toFloat(int value)
    {
        return toFloat(value / 255.0);
    }
    
    public static double toFloat(double value)
    {
        return value < 0 ? 0F : Math.min(value, 1.0);
    }
    
    // -------------------- Instance -------------------- //
    
    protected int r, g, b, a;
    
    public Color()
    {
        this.a = 255;
    }
    
    public Color(@NotNull Colorc color)
    {
        set(color);
    }
    
    public Color(int r, int g, int b, int a)
    {
        set(r, g, b, a);
    }
    
    public Color(int r, int g, int b)
    {
        set(r, g, b, 255);
    }
    
    public Color(int gray, int a)
    {
        set(gray, a);
    }
    
    public Color(int gray)
    {
        set(gray, 255);
    }
    
    public Color(double r, double g, double b, double a)
    {
        set(r, g, b, a);
    }
    
    public Color(double r, double g, double b)
    {
        set(r, g, b, 255);
    }
    
    public Color(double gray, double a)
    {
        set(gray, a);
    }
    
    public Color(double gray)
    {
        set(gray, 255);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.r, this.g, this.b, this.a);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Color color = (Color) o;
        return this.r == color.r && this.g == color.g && this.b == color.b && this.a == color.a;
    }
    
    @Override
    public String toString()
    {
        return "Color{r=" + this.r + ", g=" + this.g + ", b=" + this.b + ", a=" + this.a + '}';
    }
    
    @Override
    public boolean equals(int r, int g, int b, int a)
    {
        return this.r == Color.toInt(r) && this.g == Color.toInt(g) && this.b == Color.toInt(b) && this.a == Color.toInt(a);
    }
    
    @Override
    public boolean equals(int r, int g, int b)
    {
        return this.r == Color.toInt(r) && this.g == Color.toInt(g) && this.b == Color.toInt(b);
    }
    
    @Override
    public boolean equals(int gray, int a)
    {
        return Color.toGray(this.r, this.g, this.b) == Color.toInt(gray) && this.a == Color.toInt(a);
    }
    
    @Override
    public boolean equals(int gray)
    {
        return Color.toGray(this.r, this.g, this.b) == Color.toInt(gray);
    }
    
    @Override
    public boolean equals(double r, double g, double b, double a)
    {
        return this.r == Color.toInt(r) && this.g == Color.toInt(g) && this.b == Color.toInt(b) && this.a == Color.toInt(a);
    }
    
    @Override
    public boolean equals(double r, double g, double b)
    {
        return this.r == Color.toInt(r) && this.g == Color.toInt(g) && this.b == Color.toInt(b);
    }
    
    @Override
    public boolean equals(double gray, double a)
    {
        return Color.toGray(this.r, this.g, this.b) == Color.toInt(gray) && this.a == Color.toInt(a);
    }
    
    @Override
    public boolean equals(double gray)
    {
        return Color.toGray(this.r, this.g, this.b) == Color.toInt(gray);
    }
    
    @Override
    public int r()
    {
        return this.r;
    }
    
    @Override
    public int g()
    {
        return this.g;
    }
    
    @Override
    public int b()
    {
        return this.b;
    }
    
    @Override
    public int a()
    {
        return this.a;
    }
    
    @Override
    public float rf()
    {
        return (float) Color.toFloat(this.r);
    }
    
    @Override
    public float gf()
    {
        return (float) Color.toFloat(this.g);
    }
    
    @Override
    public float bf()
    {
        return (float) Color.toFloat(this.b);
    }
    
    @Override
    public float af()
    {
        return (float) Color.toFloat(this.a);
    }
    
    public @NotNull Color r(int r)
    {
        this.r = Color.toInt(r);
        return this;
    }
    
    public @NotNull Color g(int g)
    {
        this.g = Color.toInt(g);
        return this;
    }
    
    public @NotNull Color b(int b)
    {
        this.b = Color.toInt(b);
        return this;
    }
    
    public @NotNull Color a(int a)
    {
        this.a = Color.toInt(a);
        return this;
    }
    
    public @NotNull Color r(double r)
    {
        this.r = Color.toInt(r);
        return this;
    }
    
    public @NotNull Color g(double g)
    {
        this.g = Color.toInt(g);
        return this;
    }
    
    public @NotNull Color b(double b)
    {
        this.b = Color.toInt(b);
        return this;
    }
    
    public @NotNull Color a(double a)
    {
        this.a = Color.toInt(a);
        return this;
    }
    
    public @NotNull Color set(@NotNull Colorc color)
    {
        this.r = color.r();
        this.g = color.g();
        this.b = color.b();
        this.a = color.a();
        return this;
    }
    
    public @NotNull Color set(int r, int g, int b, int a)
    {
        this.r = Color.toInt(r);
        this.g = Color.toInt(g);
        this.b = Color.toInt(b);
        this.a = Color.toInt(a);
        return this;
    }
    
    public @NotNull Color set(int r, int g, int b)
    {
        this.r = Color.toInt(r);
        this.g = Color.toInt(g);
        this.b = Color.toInt(b);
        return this;
    }
    
    public @NotNull Color set(int gray, int a)
    {
        int g = Color.toInt(gray);
        this.r = g;
        this.g = g;
        this.b = g;
        this.a = Color.toInt(a);
        return this;
    }
    
    public @NotNull Color set(int gray)
    {
        int g = Color.toInt(gray);
        this.r = g;
        this.g = g;
        this.b = g;
        return this;
    }
    
    public @NotNull Color set(double r, double g, double b, double a)
    {
        this.r = Color.toInt(r);
        this.g = Color.toInt(g);
        this.b = Color.toInt(b);
        this.a = Color.toInt(a);
        return this;
    }
    
    public @NotNull Color set(double r, double g, double b)
    {
        this.r = Color.toInt(r);
        this.g = Color.toInt(g);
        this.b = Color.toInt(b);
        return this;
    }
    
    public @NotNull Color set(double gray, double a)
    {
        int g = Color.toInt(gray);
        this.r = g;
        this.g = g;
        this.b = g;
        this.a = Color.toInt(a);
        return this;
    }
    
    public @NotNull Color set(double gray)
    {
        int g = Color.toInt(gray);
        this.r = g;
        this.g = g;
        this.b = g;
        return this;
    }
    
    public @NotNull Color setFromInt(int value)
    {
        this.a = (value >>> 24) & 0xFF;
        this.r = (value >>> 16) & 0xFF;
        this.g = (value >>> 8) & 0xFF;
        this.b = value & 0xFF;
        
        return this;
    }
    
    public @NotNull Color setFromHSB(double hue, double saturation, double value)
    {
        if (hue < 0.0) hue = 0.0;
        if (hue > 360.0) hue = 360.0;
        if (saturation < 0.0) saturation = 0.0;
        if (saturation > 1.0) saturation = 1.0;
        if (value < 0.0) value = 0.0;
        if (value > 1.0) value = 1.0;
        
        double k;
        
        // Red channel
        k      = (5.0 + hue / 60.0) % 6.0;
        k      = Math.min(4.0 - k, k);
        k      = k < 0 ? 0.0 : Math.min(k, 1.0);
        this.r = Color.toInt(value - value * saturation * k);
        
        // Green channel
        k      = (3.0 + hue / 60.0) % 6.0;
        k      = Math.min(4.0 - k, k);
        k      = k < 0 ? 0.0 : Math.min(k, 1.0);
        this.g = Color.toInt(value - value * saturation * k);
        
        // Blue channel
        k      = (1.0 + hue / 60.0) % 6.0;
        k      = Math.min(4.0 - k, k);
        k      = k < 0 ? 0.0 : Math.min(k, 1.0);
        this.b = Color.toInt(value - value * saturation * k);
        
        return this;
    }
    
    @Override
    public @NotNull Color tint(@NotNull Colorc color, @NotNull Color out)
    {
        out.r = toInt(this.r * color.r() / 255);
        out.g = toInt(this.g * color.g() / 255);
        out.b = toInt(this.b * color.b() / 255);
        out.a = toInt(this.a * color.a() / 255);
        return out;
    }
    
    public @NotNull Color tint(@NotNull Colorc color)
    {
        return tint(color, this);
    }
    
    @Override
    public @NotNull Color grayscale(@NotNull Color out)
    {
        int gray = Color.toGray(this.r, this.g, this.b);
        out.r = gray;
        out.g = gray;
        out.b = gray;
        out.a = this.a;
        return out;
    }
    
    public @NotNull Color grayscale()
    {
        return grayscale(this);
    }
    
    @Override
    public @NotNull Color brightness(double brightness, @NotNull Color out)
    {
        if (brightness < -1.0) brightness = -1.0;
        if (brightness > 1.0) brightness = 1.0;
        
        int b = Color.toInt(brightness);
        
        out.r = Color.toInt(this.r + b);
        out.g = Color.toInt(this.g + b);
        out.b = Color.toInt(this.b + b);
        out.a = this.a;
        
        return out;
    }
    
    public @NotNull Color brightness(double brightness)
    {
        return brightness(brightness, this);
    }
    
    @Override
    public @NotNull Color contrast(double contrast, @NotNull Color out)
    {
        if (contrast < -1.0) contrast = -1.0;
        if (contrast > 1.0) contrast = 1.0;
        
        double f = (contrast + 1.0) / (259.0 - (255.0 * contrast));
        
        out.r = (int) (f * (this.r - 128) + 128);
        out.g = (int) (f * (this.g - 128) + 128);
        out.b = (int) (f * (this.b - 128) + 128);
        out.a = this.a;
        
        return out;
    }
    
    public @NotNull Color contrast(double contrast)
    {
        return contrast(contrast, this);
    }
    
    @Override
    public @NotNull Color gamma(double gamma, @NotNull Color out)
    {
        gamma = 1.0 / gamma;
        
        out.r = Color.toInt(Math.pow(this.r / 255.0, gamma));
        out.g = Color.toInt(Math.pow(this.g / 255.0, gamma));
        out.b = Color.toInt(Math.pow(this.b / 255.0, gamma));
        out.a = this.a;
        
        return out;
    }
    
    public @NotNull Color gamma(double gamma)
    {
        return gamma(gamma, this);
    }
    
    @Override
    public @NotNull Color invert(@NotNull Color out)
    {
        out.r = 255 - this.r;
        out.g = 255 - this.g;
        out.b = 255 - this.b;
        out.a = this.a;
        
        return out;
    }
    
    public @NotNull Color invert()
    {
        return invert(this);
    }
    
    @Override
    public @NotNull Color brighter(double percentage, @NotNull Color out)
    {
        if (percentage < 0) return out.set(this);
        if (percentage > 1.0) percentage = 1.0;
        
        // percentage = 1 + percentage * (2 - percentage); // Quadratic
        percentage = 1 + percentage; // Linear
        
        out.r = Color.toInt((int) (this.r * percentage));
        out.g = Color.toInt((int) (this.g * percentage));
        out.b = Color.toInt((int) (this.b * percentage));
        out.a = this.a;
        
        return out;
    }
    
    public @NotNull Color brighter(double percentage)
    {
        return brighter(percentage, this);
    }
    
    @Override
    public @NotNull Color darker(double percentage, @NotNull Color out)
    {
        if (percentage < 0) return out.set(this);
        if (percentage > 1.0) percentage = 1.0;
        
        // percentage = 1 + percentage * (0.5 * percentage - 1); // Quadratic
        percentage = 1.0 - percentage; // Linear
        
        out.r = Color.toInt((int) (this.r * percentage));
        out.g = Color.toInt((int) (this.g * percentage));
        out.b = Color.toInt((int) (this.b * percentage));
        out.a = this.a;
        
        return out;
    }
    
    public @NotNull Color darker(double percentage)
    {
        return darker(percentage, this);
    }
    
    @Override
    public @NotNull Color interpolate(@NotNull Colorc src, double amount, @NotNull Color out)
    {
        if (amount <= 0.0) return out.set(this);
        if (amount >= 1.0) return out.set(src);
        
        int f        = (int) Math.round(amount * 255);
        int fInverse = 255 - f;
        
        out.r = toInt((this.r * fInverse + src.r() * f) / 255);
        out.g = toInt((this.g * fInverse + src.g() * f) / 255);
        out.b = toInt((this.b * fInverse + src.b() * f) / 255);
        out.a = toInt((this.a * fInverse + src.a() * f) / 255);
        
        return out;
    }
    
    public @NotNull Color interpolate(@NotNull Colorc src, double amount)
    {
        return interpolate(src, amount, this);
    }
    
    @Override
    public int toInt()
    {
        return (this.a << 24) | (this.r << 16) | (this.g << 8) | this.b;
    }
    
    @Override
    public void writeExternal(@NotNull ObjectOutput out) throws IOException
    {
        out.writeInt(this.r);
        out.writeInt(this.g);
        out.writeInt(this.b);
        out.writeInt(this.a);
    }
    
    @Override
    public void readExternal(@NotNull ObjectInput in) throws IOException
    {
        this.r = in.readInt();
        this.g = in.readInt();
        this.b = in.readInt();
        this.a = in.readInt();
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
