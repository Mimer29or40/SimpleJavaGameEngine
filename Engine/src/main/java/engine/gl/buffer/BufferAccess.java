package engine.gl.buffer;

import org.lwjgl.opengl.GL44;

public enum BufferAccess
{
    READ_ONLY(GL44.GL_READ_ONLY),
    WRITE_ONLY(GL44.GL_WRITE_ONLY),
    READ_WRITE(GL44.GL_READ_WRITE),
    ;
    
    public final int ref;
    
    BufferAccess(int ref)
    {
        this.ref = ref;
    }
}
