package engine.gl;

import org.lwjgl.opengl.GL40;

public enum StencilOp
{
    ZERO(GL40.GL_ZERO),
    
    KEEP(GL40.GL_KEEP),
    REPLACE(GL40.GL_REPLACE),
    
    INCREASE(GL40.GL_INCR),
    DECREASE(GL40.GL_DECR),
    INCREASE_WRAP(GL40.GL_INCR_WRAP),
    DECREASE_WRAP(GL40.GL_DECR_WRAP),
    
    INVERT(GL40.GL_INVERT),
    ;
    
    public final int ref;
    
    StencilOp(int ref)
    {
        this.ref = ref;
    }
}
