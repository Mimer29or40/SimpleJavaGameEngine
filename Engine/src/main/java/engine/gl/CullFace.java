package engine.gl;

import org.lwjgl.opengl.GL44;

public enum CullFace
{
    NONE(-1),
    FRONT(GL44.GL_FRONT),
    BACK(GL44.GL_BACK),
    FRONT_AND_BACK(GL44.GL_FRONT_AND_BACK),
    ;
    
    public static final CullFace DEFAULT = BACK;
    
    public final int ref;
    
    CullFace(int ref)
    {
        this.ref = ref;
    }
}
