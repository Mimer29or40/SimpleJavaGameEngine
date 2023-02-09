package engine.color;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public interface Colorc
{
    boolean equals(int r, int g, int b, int a);
    
    boolean equals(int r, int g, int b);
    
    boolean equals(int gray, int a);
    
    boolean equals(int gray);
    
    boolean equals(double r, double g, double b, double a);
    
    boolean equals(double r, double g, double b);
    
    boolean equals(double gray, double a);
    
    boolean equals(double gray);
    
    int r();
    
    int g();
    
    int b();
    
    int a();
    
    float rf();
    
    float gf();
    
    float bf();
    
    float af();
    
    @NotNull Color tint(@NotNull Colorc color, @NotNull Color out);
    
    @NotNull Color grayscale(@NotNull Color out);
    
    @NotNull Color brightness(double brightness, @NotNull Color out);
    
    @NotNull Color contrast(double contrast, @NotNull Color out);
    
    @NotNull Color gamma(double gamma, @NotNull Color out);
    
    @NotNull Color invert(@NotNull Color out);
    
    @NotNull Color brighter(double percentage, @NotNull Color out);
    
    @NotNull Color darker(double percentage, @NotNull Color out);
    
    @NotNull Color interpolate(@NotNull Colorc src, double amount, @NotNull Color out);
    
    int toInt();
    
    default @NotNull Vector3d toHSV(@NotNull Vector3d result)
    {
        double r = rf();
        double g = gf();
        double b = bf();
    
        double min = Math.min(Math.min(r, g), b);
        double max = Math.max(Math.max(r, g), b);
    
        double delta = max - min;
    
        result.z = max; // Value
    
        if (max > 0.0)
        {
            // NOTE: If max is 0, this divide would cause a crash
            result.y = delta / max; // Saturation
        }
        else
        {
            // NOTE: If max is 0, then r = g = b = 0, s = 0, h is undefined
            result.x = 0.0; // Hue can be any value, defaults to 0
            result.y = 0.0;
            return result;
        }
    
        // NOTE: Comparing float values could not work properly
        if (r >= max)
        {
            result.x = (g - b) / delta; // Between yellow & magenta
        }
        else if (g >= max)
        {
            result.x = 2.0 + (b - r) / delta; // Between cyan & yellow
        }
        else
        {
            result.x = 4.0 + (r - g) / delta; // Between magenta & cyan
        }
    
        result.x *= 60.0; // Convert to degrees
    
        if (result.x < 0.0) result.x += 360.0;
    
        return result;
    }
    
    default @NotNull Vector3d toHSV()
    {
        return toHSV(new Vector3d());
    }
}
