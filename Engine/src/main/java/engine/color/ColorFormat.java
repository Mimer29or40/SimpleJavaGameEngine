package engine.color;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL40;

public enum ColorFormat
{
    UNKNOWN(0, false, -1, -1),
    RED(1, false, GL40.GL_RED, GL40.GL_R8),
    RED_ALPHA(2, true, GL40.GL_RG, GL40.GL_RG8),
    RGB(3, false, GL40.GL_RGB, GL40.GL_RGB8),
    RGBA(4, true, GL40.GL_RGBA, GL40.GL_RGBA8),
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
    
    ColorFormat(int sizeof, boolean alpha, int format, int internalFormat)
    {
        this.sizeof         = sizeof;
        this.alpha          = alpha;
        this.format         = format;
        this.internalFormat = internalFormat;
    }
}
