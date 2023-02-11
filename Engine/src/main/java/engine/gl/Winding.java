package engine.gl;

import org.lwjgl.opengl.GL40;

public enum Winding
{
    CCW(GL40.GL_CCW),
    CW(GL40.GL_CW),
    ;
    
    public static final Winding DEFAULT = CCW;
    
    public final int ref;
    
    Winding(int ref)
    {
        this.ref = ref;
    }
}
