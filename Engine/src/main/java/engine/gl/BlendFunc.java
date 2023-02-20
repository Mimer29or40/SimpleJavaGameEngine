package engine.gl;

import org.lwjgl.opengl.GL44;

public enum BlendFunc
{
    //@formatter:off
    ZERO(GL44.GL_ZERO),
    ONE(GL44.GL_ONE),
    
    SRC_COLOR(GL44.GL_SRC_COLOR),
    ONE_MINUS_SRC_COLOR(GL44.GL_ONE_MINUS_SRC_COLOR),
    SRC_ALPHA(GL44.GL_SRC_ALPHA),
    ONE_MINUS_SRC_ALPHA(GL44.GL_ONE_MINUS_SRC_ALPHA),
    
    DST_COLOR(GL44.GL_DST_COLOR),
    ONE_MINUS_DST_COLOR(GL44.GL_ONE_MINUS_DST_COLOR),
    DST_ALPHA(GL44.GL_DST_ALPHA),
    ONE_MINUS_DST_ALPHA(GL44.GL_ONE_MINUS_DST_ALPHA),
    ;
    
    public final int ref;
    
    BlendFunc(int ref)
    {
        this.ref = ref;
    }
}
