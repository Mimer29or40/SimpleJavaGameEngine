package engine.gl;

import org.lwjgl.opengl.GL44;

public enum StencilOp
{
    ZERO(GL44.GL_ZERO),
    
    KEEP(GL44.GL_KEEP),
    REPLACE(GL44.GL_REPLACE),
    
    INCREASE(GL44.GL_INCR),
    DECREASE(GL44.GL_DECR),
    INCREASE_WRAP(GL44.GL_INCR_WRAP),
    DECREASE_WRAP(GL44.GL_DECR_WRAP),
    
    INVERT(GL44.GL_INVERT),
    ;
    
    public final int ref;
    
    StencilOp(int ref)
    {
        this.ref = ref;
    }
}
