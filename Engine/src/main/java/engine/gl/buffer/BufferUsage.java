package engine.gl.buffer;

import org.lwjgl.opengl.GL40;

public enum BufferUsage
{
    STREAM_DRAW(GL40.GL_STREAM_DRAW),
    STREAM_READ(GL40.GL_STREAM_READ),
    STREAM_COPY(GL40.GL_STREAM_COPY),
    STATIC_DRAW(GL40.GL_STATIC_DRAW),
    STATIC_READ(GL40.GL_STATIC_READ),
    STATIC_COPY(GL40.GL_STATIC_COPY),
    DYNAMIC_DRAW(GL40.GL_DYNAMIC_DRAW),
    DYNAMIC_READ(GL40.GL_DYNAMIC_READ),
    DYNAMIC_COPY(GL40.GL_DYNAMIC_COPY),
    ;
    
    public final int ref;
    
    BufferUsage(int ref)
    {
        this.ref = ref;
    }
}
