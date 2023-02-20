package engine.gl;

import org.lwjgl.opengl.GL44;

public enum DepthMode
{
    NONE(-1, (s, d) -> false),
    
    NEVER(GL44.GL_NEVER, (s, d) -> false),
    ALWAYS(GL44.GL_ALWAYS, (s, d) -> true),
    
    EQUAL(GL44.GL_EQUAL, (s, d) -> Double.compare(s, d) == 0),
    NOT_EQUAL(GL44.GL_NOTEQUAL, (s, d) -> Double.compare(s, d) != 0),
    
    LESS(GL44.GL_LESS, (s, d) -> Double.compare(s, d) < 0),
    L_EQUAL(GL44.GL_LEQUAL, (s, d) -> Double.compare(s, d) <= 0),
    G_EQUAL(GL44.GL_GEQUAL, (s, d) -> Double.compare(s, d) >= 0),
    GREATER(GL44.GL_GREATER, (s, d) -> Double.compare(s, d) > 0),
    ;
    
    public static final DepthMode DEFAULT = LESS;
    
    public final  int        ref;
    private final IDepthFunc func;
    
    DepthMode(int ref, IDepthFunc func)
    {
        this.ref  = ref;
        this.func = func;
    }
    
    public boolean test(double s, double d)
    {
        return this.func.test(s, d);
    }
    
    private interface IDepthFunc
    {
        boolean test(double s, double d);
    }
}
