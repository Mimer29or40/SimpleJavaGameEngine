package engine.gl;

import static org.lwjgl.opengl.GL44.*;

public enum BlendFunc
{
    ZERO(GL_ZERO),
    ONE(GL_ONE),
    
    SRC_COLOR(GL_SRC_COLOR),
    ONE_MINUS_SRC_COLOR(GL_ONE_MINUS_SRC_COLOR),
    SRC_ALPHA(GL_SRC_ALPHA),
    ONE_MINUS_SRC_ALPHA(GL_ONE_MINUS_SRC_ALPHA),
    
    DST_COLOR(GL_DST_COLOR),
    ONE_MINUS_DST_COLOR(GL_ONE_MINUS_DST_COLOR),
    DST_ALPHA(GL_DST_ALPHA),
    ONE_MINUS_DST_ALPHA(GL_ONE_MINUS_DST_ALPHA),
    ;
    
    public final int ref;
    
    BlendFunc(int ref)
    {
        this.ref = ref;
    }
}
