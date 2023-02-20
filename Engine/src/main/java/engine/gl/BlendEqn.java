package engine.gl;

import org.lwjgl.opengl.GL44;

public enum BlendEqn
{
    //@formatter:off
    ADD(GL44.GL_FUNC_ADD),
    
    SUBTRACT(GL44.GL_FUNC_SUBTRACT),
    REVERSE_SUBTRACT(GL44.GL_FUNC_REVERSE_SUBTRACT),
    
    MIN(GL44.GL_MIN),
    MAX(GL44.GL_MAX),
    ;
    
    public final int ref;
    
    BlendEqn(int ref)
    {
        this.ref = ref;
    }
}
