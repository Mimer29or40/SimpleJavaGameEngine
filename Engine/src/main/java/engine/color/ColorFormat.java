package engine.color;

import org.jetbrains.annotations.NotNull;

import static org.lwjgl.opengl.GL44.*;

public enum ColorFormat
{
    UNKNOWN(0, false, -1, -1, -1),
    RED(1, false, GL_RED, GL_R8, GL_SLUMINANCE),
    RED_ALPHA(2, true, GL_RG, GL_RG8, GL_SLUMINANCE_ALPHA),
    RGB(3, false, GL_RGB, GL_RGB8, GL_SRGB),
    RGBA(4, true, GL_RGBA, GL_RGBA8, GL_SRGB_ALPHA),
    ;
    
    public static final ColorFormat DEFAULT = RGBA;
    
    public static @NotNull ColorFormat get(int channels)
    {
        return switch (channels)
                {
                    case 1 -> ColorFormat.RED;
                    case 2 -> ColorFormat.RED_ALPHA;
                    case 3 -> ColorFormat.RGB;
                    case 4 -> ColorFormat.RGBA;
                    default -> ColorFormat.UNKNOWN;
                };
    }
    
    public final int     sizeof;
    public final boolean alpha;
    public final int     format;
    public final int     internalFormat;
    public final int     gammaFormat;
    
    ColorFormat(int sizeof, boolean alpha, int format, int internalFormat, int gammaFormat)
    {
        this.sizeof         = sizeof;
        this.alpha          = alpha;
        this.format         = format;
        this.internalFormat = internalFormat;
        this.gammaFormat    = gammaFormat;
    }
}
