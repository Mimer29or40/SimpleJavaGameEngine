package engine.gl;

import static org.lwjgl.opengl.GL44.*;

public enum StencilOp
{
    ZERO(GL_ZERO),
    
    KEEP(GL_KEEP),
    REPLACE(GL_REPLACE),
    
    INCREASE(GL_INCR),
    DECREASE(GL_DECR),
    INCREASE_WRAP(GL_INCR_WRAP),
    DECREASE_WRAP(GL_DECR_WRAP),
    
    INVERT(GL_INVERT),
    ;
    
    public final int ref;
    
    StencilOp(int ref)
    {
        this.ref = ref;
    }
}
