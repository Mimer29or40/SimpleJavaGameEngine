package engine.gl;

import org.lwjgl.opengl.GL40;

public enum CullFace
{
    NONE(-1),
    FRONT(GL40.GL_FRONT),
    BACK(GL40.GL_BACK),
    FRONT_AND_BACK(GL40.GL_FRONT_AND_BACK),
    ;
    
    public static final CullFace DEFAULT = BACK;
    
    public final int ref;
    
    CullFace(int ref)
    {
        this.ref = ref;
    }
}
