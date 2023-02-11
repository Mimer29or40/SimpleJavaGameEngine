package engine.gl;

import org.lwjgl.opengl.GL44;

public enum Winding
{
    CCW(GL44.GL_CCW),
    CW(GL44.GL_CW),
    ;
    
    public static final Winding DEFAULT = CCW;
    
    public final int ref;
    
    Winding(int ref)
    {
        this.ref = ref;
    }
}
