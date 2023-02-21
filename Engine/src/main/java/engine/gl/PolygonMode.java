package engine.gl;

import static org.lwjgl.opengl.GL44.*;

public enum PolygonMode
{
    POINT(GL_POINT),
    LINE(GL_LINE),
    FILL(GL_FILL),
    ;
    
    public static final PolygonMode DEFAULT = FILL;
    
    public final int ref;
    
    PolygonMode(int ref)
    {
        this.ref = ref;
    }
}
