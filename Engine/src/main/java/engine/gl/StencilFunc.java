package engine.gl;

import org.lwjgl.opengl.GL44;

public enum StencilFunc
{
    NEVER(GL44.GL_NEVER),
    ALWAYS(GL44.GL_ALWAYS),
    
    EQUAL(GL44.GL_EQUAL),
    NOT_EQUAL(GL44.GL_NOTEQUAL),
    
    LESS(GL44.GL_LESS),
    L_EQUAL(GL44.GL_LEQUAL),
    G_EQUAL(GL44.GL_GEQUAL),
    GREATER(GL44.GL_GREATER),
    ;
    
    public final int ref;
    
    StencilFunc(int ref)
    {
        this.ref = ref;
    }
}
