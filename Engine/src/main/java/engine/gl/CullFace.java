package engine.gl;

import static org.lwjgl.opengl.GL44.*;

public enum CullFace
{
    NONE(-1),
    FRONT(GL_FRONT),
    BACK(GL_BACK),
    FRONT_AND_BACK(GL_FRONT_AND_BACK),
    ;
    
    public static final CullFace DEFAULT = NONE;
    
    public final int ref;
    
    CullFace(int ref)
    {
        this.ref = ref;
    }
}
