package engine.color;

import org.jetbrains.annotations.NotNull;

import static org.lwjgl.opengl.GL44.*;

public enum ColorFormat
{
    UNKNOWN(0, false, -1, -1, -1, -1),
    
    RED(1, false, GL_RED, GL_R8, GL_SLUMINANCE, GL_UNSIGNED_BYTE),
    RED_ALPHA(2, true, GL_RG, GL_RG8, GL_SLUMINANCE_ALPHA, GL_UNSIGNED_BYTE),
    RGB(3, false, GL_RGB, GL_RGB8, GL_SRGB, GL_UNSIGNED_BYTE),
    RGBA(4, true, GL_RGBA, GL_RGBA8, GL_SRGB_ALPHA, GL_UNSIGNED_BYTE),
    
    STENCIL(1, false, GL_STENCIL_INDEX, GL_STENCIL_INDEX8, GL_STENCIL_INDEX8, GL_UNSIGNED_BYTE),
    DEPTH(3, false, GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT24, GL_UNSIGNED_BYTE),
    DEPTH_STENCIL(4, false, GL_DEPTH_STENCIL, GL_DEPTH24_STENCIL8, GL_DEPTH24_STENCIL8, GL_UNSIGNED_INT_24_8),
    
    RGB_16F(3, false, GL_RGB, GL_RGB16F, GL_RGB16F, GL_FLOAT),
    RGBA_16F(4, true, GL_RGBA, GL_RGBA16F, GL_RGBA16F, GL_FLOAT),
    RGB_32F(3, false, GL_RGB, GL_RGB32F, GL_RGB16F, GL_FLOAT),
    RGBA_32F(4, true, GL_RGBA, GL_RGBA32F, GL_RGBA16F, GL_FLOAT),
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
    public final int     dataType;
    
    ColorFormat(int sizeof, boolean alpha, int format, int internalFormat, int gammaFormat, int dataType)
    {
        this.sizeof         = sizeof;
        this.alpha          = alpha;
        this.format         = format;
        this.internalFormat = internalFormat;
        this.gammaFormat    = gammaFormat;
        this.dataType       = dataType;
    }
}
