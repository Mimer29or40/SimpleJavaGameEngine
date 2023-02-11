package engine.gl;

import org.lwjgl.opengl.GL44;

public enum ScreenBuffer
{
    COLOR(GL44.GL_COLOR_BUFFER_BIT),
    DEPTH(GL44.GL_DEPTH_BUFFER_BIT),
    STENCIL(GL44.GL_STENCIL_BUFFER_BIT),
    ;
    
    public final int ref;
    
    ScreenBuffer(int ref)
    {
        this.ref = ref;
    }
}
