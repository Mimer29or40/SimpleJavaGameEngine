package engine.gl.texture;

import static org.lwjgl.opengl.GL44.*;

public enum TextureWrap
{
    CLAMP_TO_EDGE(GL_CLAMP_TO_EDGE),
    CLAMP_TO_BORDER(GL_CLAMP_TO_BORDER),
    MIRRORED_REPEAT(GL_MIRRORED_REPEAT),
    REPEAT(GL_REPEAT),
    MIRROR_CLAMP_TO_EDGE(GL_MIRROR_CLAMP_TO_EDGE),
    ;
    
    public static final TextureWrap DEFAULT = REPEAT;
    
    public final int ref;
    
    TextureWrap(int ref)
    {
        this.ref = ref;
    }
}
