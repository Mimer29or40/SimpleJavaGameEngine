package engine.gl;

import org.lwjgl.opengl.GL40;

public enum StencilFunc
{
    NEVER(GL40.GL_NEVER),
    ALWAYS(GL40.GL_ALWAYS),
    
    EQUAL(GL40.GL_EQUAL),
    NOT_EQUAL(GL40.GL_NOTEQUAL),
    
    LESS(GL40.GL_LESS),
    L_EQUAL(GL40.GL_LEQUAL),
    G_EQUAL(GL40.GL_GEQUAL),
    GREATER(GL40.GL_GREATER),
    ;
    
    public final int ref;
    
    StencilFunc(int ref)
    {
        this.ref = ref;
    }
}
