package engine.gl;

import static org.lwjgl.opengl.GL44.GL_CCW;
import static org.lwjgl.opengl.GL44.GL_CW;

public enum Winding
{
    CCW(GL_CCW),
    CW(GL_CW),
    ;
    
    public static final Winding DEFAULT = CCW;
    
    public final int ref;
    
    Winding(int ref)
    {
        this.ref = ref;
    }
}
