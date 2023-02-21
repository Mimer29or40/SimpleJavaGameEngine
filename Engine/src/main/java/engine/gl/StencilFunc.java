package engine.gl;

import static org.lwjgl.opengl.GL44.*;

public enum StencilFunc
{
    NEVER(GL_NEVER),
    ALWAYS(GL_ALWAYS),
    
    EQUAL(GL_EQUAL),
    NOT_EQUAL(GL_NOTEQUAL),
    
    LESS(GL_LESS),
    L_EQUAL(GL_LEQUAL),
    G_EQUAL(GL_GEQUAL),
    GREATER(GL_GREATER),
    ;
    
    public final int ref;
    
    StencilFunc(int ref)
    {
        this.ref = ref;
    }
}
