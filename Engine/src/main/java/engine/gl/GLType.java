package engine.gl;

import org.lwjgl.opengl.GL44;

public enum GLType
{
    BYTE(GL44.GL_BYTE, true, Byte.BYTES),
    UNSIGNED_BYTE(GL44.GL_UNSIGNED_BYTE, false, Byte.BYTES),
    
    SHORT(GL44.GL_SHORT, true, Short.BYTES),
    UNSIGNED_SHORT(GL44.GL_UNSIGNED_SHORT, false, Short.BYTES),
    
    INT(GL44.GL_INT, true, Integer.BYTES),
    UNSIGNED_INT(GL44.GL_UNSIGNED_INT, false, Integer.BYTES),
    
    // UNSIGNED_INT_2_10_10_10_REV(GL44.GL_UNSIGNED_INT_2_10_10_10_REV, false, 4),
    // INT_2_10_10_10_REV(GL44.GL_INT_2_10_10_10_REV, true, 4),
    
    FLOAT(GL44.GL_FLOAT, true, Float.BYTES),
    DOUBLE(GL44.GL_DOUBLE, true, Double.BYTES),
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
