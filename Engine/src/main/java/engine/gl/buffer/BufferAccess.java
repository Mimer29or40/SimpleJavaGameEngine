package engine.gl.buffer;

import org.lwjgl.opengl.GL40;

public enum BufferAccess
{
    READ_ONLY(GL40.GL_READ_ONLY),
    WRITE_ONLY(GL40.GL_WRITE_ONLY),
    READ_WRITE(GL40.GL_READ_WRITE),
    ;
    
    public final int ref;
    
    BufferAccess(int ref)
    {
        this.ref = ref;
    }
}
