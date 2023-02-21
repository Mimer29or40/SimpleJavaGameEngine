package engine.gl;

import static org.lwjgl.opengl.GL44.*;

public enum GLType
{
    BYTE(GL_BYTE, true, Byte.BYTES),
    UNSIGNED_BYTE(GL_UNSIGNED_BYTE, false, Byte.BYTES),
    
    SHORT(GL_SHORT, true, Short.BYTES),
    UNSIGNED_SHORT(GL_UNSIGNED_SHORT, false, Short.BYTES),
    
    INT(GL_INT, true, Integer.BYTES),
    UNSIGNED_INT(GL_UNSIGNED_INT, false, Integer.BYTES),
    
    // UNSIGNED_INT_2_10_10_10_REV(GL_UNSIGNED_INT_2_10_10_10_REV, false, 4),
    // INT_2_10_10_10_REV(GL_INT_2_10_10_10_REV, true, 4),
    
    FLOAT(GL_FLOAT, true, Float.BYTES),
    DOUBLE(GL_DOUBLE, true, Double.BYTES),
    ;
    
    public final int     ref;
    public final boolean signed;
    public final int     bytes;
    
    GLType(int ref, boolean signed, int bytes)
    {
        this.ref    = ref;
        this.signed = signed;
        this.bytes  = bytes;
    }
}
