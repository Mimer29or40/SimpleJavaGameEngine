package engine.gl;

import static org.lwjgl.opengl.GL44.*;

public enum BlendEqn
{
    ADD(GL_FUNC_ADD),
    
    SUBTRACT(GL_FUNC_SUBTRACT),
    REVERSE_SUBTRACT(GL_FUNC_REVERSE_SUBTRACT),
    
    MIN(GL_MIN),
    MAX(GL_MAX),
    ;
    
    public final int ref;
    
    BlendEqn(int ref)
    {
        this.ref = ref;
    }
}
