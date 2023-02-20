package engine.gl;

import org.lwjgl.opengl.GL44;

public enum PolygonMode
{
    POINT(GL44.GL_POINT),
    LINE(GL44.GL_LINE),
    FILL(GL44.GL_FILL),
    ;
    
    public static final PolygonMode DEFAULT = FILL;
    
    public final int ref;
    
    PolygonMode(int ref)
    {
        this.ref = ref;
    }
}
