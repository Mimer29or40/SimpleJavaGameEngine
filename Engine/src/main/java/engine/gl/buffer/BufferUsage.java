package engine.gl.buffer;

import org.lwjgl.opengl.GL44;

public enum BufferUsage
{
    STREAM_DRAW(GL44.GL_STREAM_DRAW),
    STREAM_READ(GL44.GL_STREAM_READ),
    STREAM_COPY(GL44.GL_STREAM_COPY),
    STATIC_DRAW(GL44.GL_STATIC_DRAW),
    STATIC_READ(GL44.GL_STATIC_READ),
    STATIC_COPY(GL44.GL_STATIC_COPY),
    DYNAMIC_DRAW(GL44.GL_DYNAMIC_DRAW),
    DYNAMIC_READ(GL44.GL_DYNAMIC_READ),
    DYNAMIC_COPY(GL44.GL_DYNAMIC_COPY),
    ;
    
    public final int ref;
    
    BufferUsage(int ref)
    {
        this.ref = ref;
    }
}
