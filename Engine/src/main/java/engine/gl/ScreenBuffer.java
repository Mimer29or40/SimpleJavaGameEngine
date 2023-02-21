package engine.gl;

import static org.lwjgl.opengl.GL44.*;

public enum ScreenBuffer
{
    COLOR(GL_COLOR_BUFFER_BIT),
    DEPTH(GL_DEPTH_BUFFER_BIT),
    STENCIL(GL_STENCIL_BUFFER_BIT),
    ;
    
    public final int ref;
    
    ScreenBuffer(int ref)
    {
        this.ref = ref;
    }
}
