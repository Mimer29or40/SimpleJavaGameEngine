package engine.gl;

import org.lwjgl.opengl.GL40;

public enum BlendFunc
{
    //@formatter:off
    ZERO(GL40.GL_ZERO),
    ONE(GL40.GL_ONE),
    
    SRC_COLOR(GL40.GL_SRC_COLOR),
    ONE_MINUS_SRC_COLOR(GL40.GL_ONE_MINUS_SRC_COLOR),
    SRC_ALPHA(GL40.GL_SRC_ALPHA),
    ONE_MINUS_SRC_ALPHA(GL40.GL_ONE_MINUS_SRC_ALPHA),
    
    DST_COLOR(GL40.GL_DST_COLOR),
    ONE_MINUS_DST_COLOR(GL40.GL_ONE_MINUS_DST_COLOR),
    DST_ALPHA(GL40.GL_DST_ALPHA),
    ONE_MINUS_DST_ALPHA(GL40.GL_ONE_MINUS_DST_ALPHA),
    ;
    
    public final int ref;
    
    BlendFunc(int ref)
    {
        this.ref = ref;
    }
}
