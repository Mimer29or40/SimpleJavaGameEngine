package engine.gl;

import org.lwjgl.opengl.GL40;

public enum BlendEqn
{
    //@formatter:off
    ADD(GL40.GL_FUNC_ADD),
    
    SUBTRACT(GL40.GL_FUNC_SUBTRACT),
    REVERSE_SUBTRACT(GL40.GL_FUNC_REVERSE_SUBTRACT),
    
    MIN(GL40.GL_MIN),
    MAX(GL40.GL_MAX),
    ;
    
    public final int ref;
    
    BlendEqn(int ref)
    {
        this.ref = ref;
    }
}
