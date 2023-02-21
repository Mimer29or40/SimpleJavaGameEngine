package engine.gl.texture;

import static org.lwjgl.opengl.GL44.*;

public enum TextureFilter
{
    NEAREST(GL_NEAREST),
    LINEAR(GL_LINEAR),
    NEAREST_MIPMAP_NEAREST(GL_NEAREST_MIPMAP_NEAREST),
    LINEAR_MIPMAP_NEAREST(GL_LINEAR_MIPMAP_NEAREST),
    NEAREST_MIPMAP_LINEAR(GL_NEAREST_MIPMAP_LINEAR),
    LINEAR_MIPMAP_LINEAR(GL_LINEAR_MIPMAP_LINEAR),
    ;
    
    public static final TextureFilter DEFAULT = NEAREST;
    
    public final int ref;
    
    TextureFilter(int ref)
    {
        this.ref = ref;
    }
}
