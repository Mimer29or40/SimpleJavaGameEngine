package engine.gl;

import org.lwjgl.opengl.GL40;

public enum ScreenBuffer
{
    COLOR(GL40.GL_COLOR_BUFFER_BIT),
    DEPTH(GL40.GL_DEPTH_BUFFER_BIT),
    STENCIL(GL40.GL_STENCIL_BUFFER_BIT),
    ;
    
    public final int ref;
    
    ScreenBuffer(int ref)
    {
        this.ref = ref;
    }
}
